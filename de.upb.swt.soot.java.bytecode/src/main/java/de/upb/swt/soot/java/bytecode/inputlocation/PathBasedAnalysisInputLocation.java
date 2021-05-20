package de.upb.swt.soot.java.bytecode.inputlocation;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalNotification;
import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.frontend.AsmJavaClassProvider;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.types.JavaClassType;
import de.upb.swt.soot.java.core.views.JavaView;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
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
   * Creates a {@link PathBasedAnalysisInputLocation} depending on the given {@link Path}, e.g.,
   * differs between directories, archives (and possibly network path's in the future).
   *
   * @param path The path to search in
   * @param javaVersion target version to analyze a multi-release jar
   * @return A {@link PathBasedAnalysisInputLocation} implementation dependent on the given {@link
   *     Path}'s FileSystem
   */
  public static @Nonnull PathBasedAnalysisInputLocation createForClassContainer(
      @Nonnull Path path) {

    if (Files.isDirectory(path)) {
      return new DirectoryBasedAnalysisInputLocation(path);
    } else if (PathUtils.isArchive(path)) {
      if (PathUtils.hasExtension(path, FileType.WAR)) {
        return new WarArchiveAnalysisInputLocation(path);
      }

      // check if mainfest contains multi release flag
      if (isMultiReleaseJar(path)) {
        return new MultiReleaseJarAnalysisInputLocation(path);
      }
      return new ArchiveBasedAnalysisInputLocation(path);
    } else {
      throw new IllegalArgumentException(
          "Path has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, war etc.");
    }
  }

  private static boolean isMultiReleaseJar(Path path) {
    try {
      FileInputStream inputStream = new FileInputStream(path.toFile());
      JarInputStream jarStream = new JarInputStream(inputStream);
      Manifest mf = jarStream.getManifest();

      if (mf == null) {
        return false;
      }

      Attributes attributes = mf.getMainAttributes();

      String value = attributes.getValue("Multi-Release");

      return Boolean.TRUE.equals(Boolean.valueOf(value));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Nonnull
  Collection<? extends AbstractClassSource<JavaSootClass>> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory factory,
      @Nonnull ClassProvider<JavaSootClass> classProvider) {
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
  protected Optional<? extends AbstractClassSource<JavaSootClass>> getClassSourceInternal(
      @Nonnull JavaClassType signature,
      @Nonnull Path path,
      @Nonnull ClassProvider<JavaSootClass> classProvider) {
    Path pathToClass =
        path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
  }

  private static class DirectoryBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    private DirectoryBasedAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
        @Nonnull IdentifierFactory identifierFactory, @Nonnull View<?> view) {
      return walkDirectory(
          path, identifierFactory, new AsmJavaClassProvider(view.getBodyInterceptors()));
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
        @Nonnull ClassType type, @Nonnull View<?> view) {
      return getClassSourceInternal(
          (JavaClassType) type, path, new AsmJavaClassProvider(view.getBodyInterceptors()));
    }
  }

  private static class MultiReleaseJarAnalysisInputLocation
      extends ArchiveBasedAnalysisInputLocation {

    private MultiReleaseJarAnalysisInputLocation(@Nonnull Path path) {
      super(path);
    }

    @Nonnull
    protected Optional<? extends AbstractClassSource<JavaSootClass>> getClassSourceInternal(
        @Nonnull JavaClassType signature,
        @Nonnull Path path,
        @Nonnull ClassProvider<JavaSootClass> classProvider,
        @Nonnull View<?> view) {

      int[] availableVersions = {};

      try {
        availableVersions =
            Files.list(path.getFileSystem().getPath("/META-INF/versions/"))
                .map(
                    dir ->
                        dir.getFileName()
                            .toString()
                            .substring(0, dir.getFileName().toString().length() - 1))
                .mapToInt(Integer::new)
                .sorted()
                .toArray();
      } catch (IOException e) {
        e.printStackTrace();
      }

      JavaView javaView = (JavaView) view;
      JavaProject javaProject = (JavaProject) javaView.getProject();
      int javaVersion = ((JavaLanguage) javaProject.getLanguage()).getVersion();

      // return best match
      for (int i = availableVersions.length - 1; i >= 0; i--) {

        if (availableVersions[i] > javaVersion) continue;

        final Path versionRoot =
            path.getFileSystem().getPath("/META-INF/versions/" + availableVersions[i] + "/");

        Path pathToClass =
            versionRoot.resolve(
                signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

        if (Files.exists(pathToClass)) {
          return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
        }
      }

      // return base class instead
      Path pathToClass =
          path.resolve(signature.toPath(classProvider.getHandledFileType(), path.getFileSystem()));

      if (!Files.exists(pathToClass)) {
        return Optional.empty();
      }

      return Optional.of(classProvider.createClassSource(this, pathToClass, signature));
    }

    @Override
    public @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
        @Nonnull ClassType type, @Nonnull View<?> view) {
      try {
        FileSystem fs = fileSystemCache.get(path);
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(
            (JavaClassType) type,
            archiveRoot,
            new AsmJavaClassProvider(view.getBodyInterceptors()),
            view);
      } catch (ExecutionException e) {
        throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
      }
    }
  }

  private static class ArchiveBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    // We cache the FileSystem instances as their creation is expensive.
    // The Guava Cache is thread-safe (see JavaDoc of LoadingCache) hence this
    // cache can be safely shared in a static variable.
    protected static final LoadingCache<Path, FileSystem> fileSystemCache =
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
    public @Nonnull Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
        @Nonnull ClassType type, @Nonnull View<?> view) {
      try {
        FileSystem fs = fileSystemCache.get(path);
        final Path archiveRoot = fs.getPath("/");
        return getClassSourceInternal(
            (JavaClassType) type,
            archiveRoot,
            new AsmJavaClassProvider(view.getBodyInterceptors()));
      } catch (ExecutionException e) {
        throw new RuntimeException("Failed to retrieve file system from cache for " + path, e);
      }
    }

    @Override
    public @Nonnull Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
        @Nonnull IdentifierFactory identifierFactory, @Nonnull View<?> view) {
      try (FileSystem fs = FileSystems.newFileSystem(path, null)) {
        final Path archiveRoot = fs.getPath("/");
        return walkDirectory(
            archiveRoot, identifierFactory, new AsmJavaClassProvider(view.getBodyInterceptors()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  // TODO: [ms] dont extractWarfile and extend ArchiveBasedAnalysisInputLocation?
  private static final class WarArchiveAnalysisInputLocation
      extends DirectoryBasedAnalysisInputLocation {
    public List<Path> jarsFromPath = new ArrayList<>();
    public static int maxExtractedSize =
        1024 * 1024 * 500; // limit of extracted file size to protect against archive bombs

    private WarArchiveAnalysisInputLocation(@Nonnull Path warPath) {
      super(
          Paths.get(
              System.getProperty("java.io.tmpdir")
                  + File.separator
                  + "sootOutput"
                  + "-war"
                  + warPath.hashCode()
                  + "/"));
      extractWarFile(warPath);
    }

    @Override
    @Nonnull
    public Collection<? extends AbstractClassSource<JavaSootClass>> getClassSources(
        @Nonnull IdentifierFactory identifierFactory, @Nonnull View<?> view) {
      List<AbstractClassSource<JavaSootClass>> foundClasses = new ArrayList<>();

      try {
        jarsFromPath =
            Files.walk(Paths.get(path.toString()))
                .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JAR))
                .flatMap(p1 -> StreamUtils.optionalToStream(Optional.of(p1)))
                .collect(Collectors.toList());
        for (Path jarPath : jarsFromPath) {
          final ArchiveBasedAnalysisInputLocation archiveBasedAnalysisInputLocation =
              new ArchiveBasedAnalysisInputLocation(jarPath);
          foundClasses.addAll(
              archiveBasedAnalysisInputLocation.getClassSources(identifierFactory, view));
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      return foundClasses;
    }

    @Override
    @Nonnull
    public Optional<? extends AbstractClassSource<JavaSootClass>> getClassSource(
        @Nonnull ClassType type, @Nonnull View<?> view) {

      try {
        jarsFromPath =
            Files.walk(Paths.get(path.toString()))
                .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JAR))
                .flatMap(p1 -> StreamUtils.optionalToStream(Optional.of(p1)))
                .collect(Collectors.toList());
        for (Path jarPath : jarsFromPath) {
          final ArchiveBasedAnalysisInputLocation archiveBasedAnalysisInputLocation =
              new ArchiveBasedAnalysisInputLocation(jarPath);
          final Optional<? extends AbstractClassSource<JavaSootClass>> classSource =
              archiveBasedAnalysisInputLocation.getClassSource(type, view);
          if (classSource.isPresent()) {
            return classSource;
          }
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }

      return Optional.empty();
    }

    /**
     * Extracts the war file at the temporary location to analyze underlying class and jar files
     *
     * @param warFilePath The path to war file to be extracted
     */
    public void extractWarFile(Path warFilePath) {
      final String destDirectory = path.toString();
      int extractedSize = 0;
      try {
        File dest = new File(destDirectory);
        if (!dest.exists()) {
          if (!dest.mkdir()) {
            throw new RuntimeException("Could not create the directory: " + destDirectory);
          }
          dest.deleteOnExit();
        }

        ZipInputStream zis = new ZipInputStream(new FileInputStream(warFilePath.toString()));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
          String filepath = destDirectory + File.separator + zipEntry.getName();
          final File file = new File(filepath);

          file.deleteOnExit();
          if (zipEntry.isDirectory()) {
            if (!file.exists()) {
              file.mkdir();
            }
          } else {
            byte[] incomingValues = new byte[4096];
            int readBytesZip;
            if (file.exists()) {
              // compare contents -> does it contain the extracted war already?
              int readBytesExistingFile;
              final BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
              byte[] bisBuf = new byte[4096];
              while ((readBytesZip = zis.read(incomingValues)) != -1) {
                if (extractedSize > maxExtractedSize) {
                  throw new RuntimeException(
                      "The extracted warfile exceeds the size of "
                          + maxExtractedSize
                          + " byte. Either the file is a big archive or maybe it contains an archive bomb.");
                }
                readBytesExistingFile = bis.read(bisBuf, 0, readBytesZip);
                if (readBytesExistingFile != readBytesZip) {
                  throw new RuntimeException(
                      "File \"" + file + "\" exists already and has differing size.");
                } else if (!Arrays.equals(bisBuf, incomingValues)) {
                  throw new RuntimeException(
                      "File \"" + file + "\" exists already and has differing contents.");
                }
                extractedSize += readBytesZip;
              }

            } else {
              BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
              while ((readBytesZip = zis.read(incomingValues)) != -1) {
                if (extractedSize > maxExtractedSize) {
                  throw new RuntimeException(
                      "The extracted warfile exceeds the size of "
                          + maxExtractedSize
                          + " byte. Either the file is a big archive or maybe it contains an archive bomb.");
                }
                bos.write(incomingValues, 0, readBytesZip);
                extractedSize += readBytesZip;
              }
              bos.close();
            }
          }
          zis.closeEntry();
        }

      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    /**
     * Parses the web.xml file to search for the servlet-class classes in the extracted directory
     * after the war file is extracted
     *
     * <p>[ms] helps to set entrypoints for analyses automatically (later)
     *
     * @param extractedWARPath The path where the war file is extracted Adds the classes associated
     *     to servlet-class in a {@link ArrayList} of {@link String}
     */
    @Nonnull
    public List<String> retrieveServletClasses(String extractedWARPath) {
      List<String> classesInXML = new ArrayList<>();
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(extractedWARPath + "/WEB-INF/web.xml"));
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
        throw new RuntimeException(e);
      }
      return classesInXML;
    }
  }
}
