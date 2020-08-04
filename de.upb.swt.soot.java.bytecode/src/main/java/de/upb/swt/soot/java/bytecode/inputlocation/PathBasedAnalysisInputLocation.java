package de.upb.swt.soot.java.bytecode.inputlocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 22.05.2018 Manuel Benz
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

/**
 * Base class for {@link PathBasedAnalysisInputLocation}s that can be located by a {@link Path}
 * object.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Kaustubh Kelkar updated on 30.07.2020
 */
public abstract class PathBasedAnalysisInputLocation implements BytecodeAnalysisInputLocation {
  protected final Path path;

  private PathBasedAnalysisInputLocation(@Nonnull Path path) {
    this.path = path;
  }

  /**
   * Return the list of the JAR files at the directory
   *
   * @param dirPath is the path for the extracted directory for the WAR file
   */
  @Nonnull
  public static List<Path> walkDirectoryForJars(@Nonnull Path dirPath) throws IOException {
    return Files.walk(dirPath)
        .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JAR))
        .flatMap(p1 -> StreamUtils.optionalToStream(Optional.of(p1)))
        .collect(Collectors.toList());
  }

  /**
   * Creates a {@link PathBasedAnalysisInputLocation} depending on the given {@link Path}, e.g.,
   * differs between directories, archives (and possibly network path's in the future).
   *
   * @param path The path to search in
   * @return A {@link PathBasedAnalysisInputLocation} implementation dependent on the given {@link
   *     Path}'s FileSystem
   */
  public static @Nonnull PathBasedAnalysisInputLocation createForClassContainer(
      @Nonnull Path path) {

    if (Files.isDirectory(path)) {
      return new DirectoryBasedAnalysisInputLocation(path);
    } else if (PathUtils.isArchive(path)) {
      if (PathUtils.hasExtension(path, FileType.WAR)) {
        return WarArchiveAnalysisInputLocation.createWarArchiveAnalysisInputLocation(path);
      }
      return new ArchiveBasedAnalysisInputLocation(path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, war etc.");
    }
  }

  @Nonnull
  Collection<? extends AbstractClassSource> walkDirectory(
      @Nonnull Path dirPath, @Nonnull IdentifierFactory factory, ClassProvider classProvider) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      return Files.walk(dirPath)
          .filter(filePath -> PathUtils.hasExtension(filePath, handledFileType))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      Optional.of(classProvider.createClassSource(this, p, factory.fromPath(p)))))
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Nonnull
  Optional<? extends AbstractClassSource> getClassSourceInternal(
      @Nonnull JavaClassType signature, @Nonnull Path path, @Nonnull ClassProvider classProvider) {
    Path pathToClass =
        path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
  }

  ClassProvider buildClassProvider(@Nonnull ClassLoadingOptions classLoadingOptions) {
    List<BodyInterceptor> bodyInterceptors = classLoadingOptions.getBodyInterceptors();
    return new AsmJavaClassProvider(bodyInterceptors);
  }

  private static class DirectoryBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    private DirectoryBasedAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {
      return walkDirectory(path, identifierFactory, buildClassProvider(classLoadingOptions));
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
      return getClassSourceInternal(
          (JavaClassType) type, path, buildClassProvider(classLoadingOptions));
    }
  }

  private static class ArchiveBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    // We cache the FileSystem instances as their creation is expensive.
    // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
    // cache can be safely shared in a static variable.
    private static final LoadingCache<Path, FileSystem> fileSystemCache =
        CacheBuilder.newBuilder()
            .removalListener(
                (RemovalNotification<Path, FileSystem> removalNotification) -> {
                  try {
                    removalNotification.getValue().close();
                  } catch (IOException e) {
                    throw new RuntimeException(
                        "Could not close file system of " + removalNotification.getKey(), e);
                  }
                })
            .expireAfterAccess(1, TimeUnit.SECONDS)
            .build(
                CacheLoader.from(
                    path -> {
                      try {
                        return FileSystems.newFileSystem(Objects.requireNonNull(path), null);
                      } catch (IOException e) {
                        throw new RuntimeException("Could not open file system of " + path, e);
                      }
                    }));

    private ArchiveBasedAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull ClassLoadingOptions classLoadingOptions) {
      try {
        FileSystem fs = fileSystemCache.get(path);
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(
            (JavaClassType) type, archiveRoot, buildClassProvider(classLoadingOptions));
      } catch (ExecutionException e) {
        throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
      }
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(
            archiveRoot, identifierFactory, buildClassProvider(classLoadingOptions));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // TODO: [ms] war is java specific -> move to soot.java module
  // TODO: [ms] dont extractWarfile and extends ArchiveBasedAnalysisInputLocation?
  private static final class WarArchiveAnalysisInputLocation
      extends DirectoryBasedAnalysisInputLocation {
    public List<Path> jarsFromPath = new ArrayList<>();

    private WarArchiveAnalysisInputLocation(@Nonnull Path extractedPath) {
      super(extractedPath);
      extractWarFile(extractedPath);
    }

    public static WarArchiveAnalysisInputLocation createWarArchiveAnalysisInputLocation(
        @Nonnull Path path) {
      String destDirectory =
          System.getProperty("java.io.tmpdir")
              + File.separator
              + "sootOutput"
              + "-war-"
              + path.hashCode();

      return new WarArchiveAnalysisInputLocation(Paths.get(destDirectory));
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource> getClassSources(
        @Nonnull IdentifierFactory identifierFactory,
        @Nonnull ClassLoadingOptions classLoadingOptions) {
      List<? extends AbstractClassSource> classesFromWar = Collections.emptyList();

      try {
        jarsFromPath = walkDirectoryForJars(Paths.get(path.toString() + "/"));
        for (Path jarPath : jarsFromPath) {
          try (FileSystem fsJar = FileSystems.newFileSystem(jarPath, null)) {
            final Path archiveRootJar = fsJar.getPath("/");
            return walkDirectory(
                archiveRootJar, identifierFactory, buildClassProvider(classLoadingOptions));
          } catch (IOException e) {
            e.getMessage();
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return classesFromWar;
    }

    /**
     * Extracts the war file at the temporary location to analyze underlying class and jar files
     *
     * @param warFilePath The path to war file to be extracted
     * @return A {@link String} location where the war file is extracted
     */
    public @Nonnull String extractWarFile(Path warFilePath) {
      // FIXME: [ms] protect against archive bombs
      final String destDirectory = warFilePath.toString();
      try {
        File dest = new File(destDirectory);
        dest.deleteOnExit();
        if (!dest.exists()) {
          dest.mkdir();
        }
        ZipInputStream zis = new ZipInputStream(new FileInputStream(warFilePath.toFile()));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
          String filepath = destDirectory + File.separator + zipEntry.getName();
          if (!zipEntry.isDirectory()) {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filepath));
            byte[] incomingValues = new byte[4096];
            int readFlag;
            while ((readFlag = zis.read(incomingValues)) != -1) {
              bos.write(incomingValues, 0, readFlag);
            }
            bos.close();
          } else {
            File newDir = new File(filepath);
            newDir.mkdir();
          }
          zis.closeEntry();
        }

      } catch (IOException e) {
        e.getMessage();
      }
      return parseWebxml(destDirectory);
    }

    /**
     * Parses the web.xml file to search for the servlet-class classes in the extracted directory
     * after the war file is extracted
     *
     * @param extractedWARPath The path where the war file is extracted Adds the classes associated
     *     to servlet-class in a {@link ArrayList} of {@link String}
     */
    public String parseWebxml(String extractedWARPath) {
      List<String> classesInXML = new ArrayList<>();
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(extractedWARPath + "WEB-INF/web.xml"));
        document.getDocumentElement().normalize();
        NodeList nList = document.getElementsByTagName("servlet");
        for (int temp = 0; temp < nList.getLength(); temp++) {
          Node node = nList.item(temp);
          if (node.getNodeType() == Node.ELEMENT_NODE) {
            Element eElement = (Element) node;
            classesInXML.add(
                eElement.getElementsByTagName("servlet-class").item(0).getTextContent());
          }
        }
      } catch (ParserConfigurationException | SAXException | IOException e) {
        e.getMessage();
      }
      return extractedWARPath;
    }
  }
}
