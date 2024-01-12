package sootup.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Manuel Benz, Christian Brüggemann, Markus Schmidt and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import com.google.common.collect.Streams;
import sootup.core.Language;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.AsmModuleSource;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.JavaModuleInfo;
import sootup.java.core.JavaSootClassSource;
import sootup.java.core.ModuleInfoAnalysisInputLocation;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.ModuleJavaClassType;

import javax.annotation.Nonnull;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * If the user wants to analyze a Multi-Release Jar, they have to specify the language level to
 * analyze explicitly. if there is no match for the given language level, the default location
 * inside the jar will be used.
 */
public class MultiReleaseJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation
        implements ModuleInfoAnalysisInputLocation {

    @Nonnull
    private final Language language;
    @Nonnull
    private final List<Integer> availableVersions;

    @Nonnull
    private final Map<Integer, Map<ModuleSignature, JavaModuleInfo>> moduleInfoMap = new HashMap<>();

    @Nonnull
    private final Map<Integer, List<AnalysisInputLocation>> inputLocations = new HashMap<>();
    @Nonnull
    private final List<AnalysisInputLocation> baseInputLocations = new ArrayList<>();

    public MultiReleaseJarAnalysisInputLocation(
            @Nonnull Path path, @Nonnull SourceType srcType, @Nonnull Language language) {
        super(path, srcType);
        this.language = language;

        if (!isMultiReleaseJar(path)) {
            throw new IllegalArgumentException("The given path does not point to a multi release jar.");
        }

        FileSystem fs;
        try {
            fs = fileSystemCache.get(path);
        } catch (ExecutionException e) {
            throw new IllegalArgumentException("Could not open filesystemcache.", e);
        }
        final Path archiveRoot = fs.getPath("/");
        Path versionedRoot = archiveRoot.getFileSystem().getPath("/META-INF/versions/");

        try (Stream<Path> list = Files.list(versionedRoot)) {
            availableVersions =
                    list.map(dir -> dir.getFileName().toString().replace("/", ""))
                            .map(Integer::new)
                            .sorted()
                            .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException("Can not index the given file.", e);
        }

        final String moduleInfoFilename = JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class";

        baseInputLocations.add(PathBasedAnalysisInputLocation.create(archiveRoot, sourceType));

        String sep = archiveRoot.getFileSystem().getSeparator();

        for (int i = availableVersions.size() - 1; i >= 0; i--) {
            Integer version = availableVersions.get(i);
            inputLocations.put(version, new ArrayList<>());

            final Path versionRoot =
                    archiveRoot.getFileSystem().getPath("/META-INF/versions/" + version + sep);

            // only versions >= 9 support java modules
            if (version > 8) {
                moduleInfoMap.put(version, new HashMap<>());
                try (DirectoryStream<Path> stream = Files.newDirectoryStream(versionRoot)) {
                    for (Path entry : stream) {

                        Path mi = path.resolve(moduleInfoFilename);

                        if (Files.exists(mi)) {
                            JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
                            ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
                            JavaModulePathAnalysisInputLocation inputLocation =
                                    new JavaModulePathAnalysisInputLocation(
                                            versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

                            inputLocations.get(version).add(inputLocation);
                            moduleInfoMap.get(version).put(moduleSignature, moduleInfo);
                        }

                        if (Files.isDirectory(entry)) {
                            mi = versionRoot.resolve(moduleInfoFilename);

                            if (Files.exists(mi)) {
                                JavaModuleInfo moduleInfo = new AsmModuleSource(mi);
                                ModuleSignature moduleSignature = moduleInfo.getModuleSignature();
                                JavaModulePathAnalysisInputLocation inputLocation =
                                        new JavaModulePathAnalysisInputLocation(
                                                versionRoot.toString(), versionRoot.getFileSystem(), getSourceType());

                                inputLocations.get(version).add(inputLocation);
                                moduleInfoMap.get(version).put(moduleSignature, moduleInfo);
                            }
                            // else TODO [bh] can we have automatic modules here?
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("Could not index the file.", e);
                }
            }

            // if there was no module or the version is not > 8, we just add a directory based input
            // location
            if (inputLocations.get(version).isEmpty()) {
                inputLocations
                        .get(version)
                        .add(PathBasedAnalysisInputLocation.create(versionRoot, sourceType));
            }
        }
    }

    @Override
    @Nonnull
    public Optional<JavaSootClassSource> getClassSource(@Nonnull ClassType type, @Nonnull View view) {

        Collection<AnalysisInputLocation> inputLocations =
                getBestMatchingInputLocationsRaw(language.getVersion());

        Collection<AnalysisInputLocation> baseInputLocations = getBaseInputLocations();

        Predicate<AnalysisInputLocation> analysisInputLocationPredicate;
        if (type instanceof ModuleJavaClassType) {
            analysisInputLocationPredicate = location -> location instanceof ModuleInfoAnalysisInputLocation;
        } else {
            analysisInputLocationPredicate = location -> !(location instanceof ModuleInfoAnalysisInputLocation);
        }

        return Streams.concat( inputLocations.stream().filter(analysisInputLocationPredicate), baseInputLocations.stream().filter(analysisInputLocationPredicate))
                .map(location -> location.getClassSource(type, view))
                .filter(Optional::isPresent)
                .limit(1)
                .map(Optional::get)
                .map(src -> (JavaSootClassSource) src)
                .findFirst();
    }

    @Nonnull
    @Override
    public Collection<JavaSootClassSource> getModulesClassSources(
            @Nonnull ModuleSignature moduleSignature, @Nonnull View view) {
        return inputLocations.get(language.getVersion()).stream()
                .filter(location -> location instanceof ModuleInfoAnalysisInputLocation)
                .map(
                        location ->
                                ((ModuleInfoAnalysisInputLocation) location)
                                        .getModulesClassSources(moduleSignature, view))
                .flatMap(Collection::stream)
                .map(src -> (JavaSootClassSource) src)
                .collect(Collectors.toList());
    }

    /**
     * Returns the best matching input locations or the base input location.
     *
     * @param javaVersion version to find best match to
     * @return best match or base input locations
     */
    private Collection<AnalysisInputLocation> getBestMatchingInputLocationsRaw(int javaVersion) {
        for (int i = availableVersions.size() - 1; i >= 0; i--) {

            Integer version = availableVersions.get(i);
            if (version > javaVersion) {
                continue;
            }

            return new ArrayList<>(inputLocations.get(version));
        }

        return getBaseInputLocations();
    }

    private Collection<AnalysisInputLocation> getBaseInputLocations() {
        return baseInputLocations;
    }

    @Override
    @Nonnull
    public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
        Collection<AnalysisInputLocation> il = getBestMatchingInputLocationsRaw(language.getVersion());

        Collection<JavaSootClassSource> result =
                il.stream()
                        .map(location -> location.getClassSources(view))
                        .flatMap(Collection::stream)
                        .map(src -> (JavaSootClassSource) src)
                        .collect(Collectors.toList());

        if (il != getBaseInputLocations()) {

            Collection<JavaSootClassSource> baseSources =
                    getBaseInputLocations().stream()
                            .map(location -> location.getClassSources(view))
                            .flatMap(Collection::stream)
                            .map(src -> (JavaSootClassSource) src)
                            .collect(Collectors.toList());

            baseSources.forEach(
                    cs -> {
                        // do not add duplicate class sources
                        if (result.stream()
                                .noneMatch(
                                        bestMatchCS ->
                                                bestMatchCS
                                                        .getClassType()
                                                        .getFullyQualifiedName()
                                                        .equals(cs.getClassType().getFullyQualifiedName()))) {
                            result.add(cs);
                        }
                    });
        }

        return result;
    }

    @Nonnull
    @Override
    public Optional<JavaModuleInfo> getModuleInfo(ModuleSignature sig, View view) {
        return Optional.ofNullable(moduleInfoMap.get(language.getVersion()).get(sig));
    }

    @Nonnull
    @Override
    public Set<ModuleSignature> getModules(@Nonnull View view) {
        return inputLocations.get(language.getVersion()).stream()
                .filter(e -> e instanceof ModuleInfoAnalysisInputLocation)
                .map(e -> ((ModuleInfoAnalysisInputLocation) e).getModules(view))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }

    @Nonnull
    public Language getLanguage() {
        return language;
    }

    @Nonnull
    public List<Integer> getAvailableVersions() {
        return availableVersions;
    }

    public static boolean isMultiReleaseJar(Path path) {
        try {
            FileInputStream inputStream = new FileInputStream(path.toFile());
            JarInputStream jarStream = new JarInputStream(inputStream);
            Manifest mf = jarStream.getManifest();

            if (mf == null) {
                return false;
            }

            Attributes attributes = mf.getMainAttributes();

            String value = attributes.getValue("Multi-Release");

            return Boolean.parseBoolean(value);
        } catch (IOException e) {
            throw new IllegalArgumentException("File not found.", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof MultiReleaseJarAnalysisInputLocation)) {
            return false;
        }
        return path.equals(((MultiReleaseJarAnalysisInputLocation) o).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
