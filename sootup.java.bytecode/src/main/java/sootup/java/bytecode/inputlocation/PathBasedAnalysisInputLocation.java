package sootup.java.bytecode.inputlocation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.ClassProvider;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.PathUtils;
import sootup.core.util.StreamUtils;
import sootup.core.views.View;
import sootup.java.bytecode.frontend.AsmJavaClassProvider;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;

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
public abstract class PathBasedAnalysisInputLocation implements AnalysisInputLocation {
  private final SourceType sourceType;
  private final List<BodyInterceptor> bodyInterceptors;
  protected Path path;

  protected PathBasedAnalysisInputLocation(@Nonnull Path path, @Nullable SourceType srcType) {
    this(path, srcType, Collections.emptyList());
  }

  protected PathBasedAnalysisInputLocation(
      @Nonnull Path path,
      @Nullable SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    this.path = path;
    this.sourceType = srcType;
    this.bodyInterceptors = bodyInterceptors;

    if (!Files.exists(path)) {
      throw new IllegalArgumentException("The provided path '" + path + "' does not exist.");
    }
  }

  @Nullable
  @Override
  public SourceType getSourceType() {
    return sourceType;
  }

  @Override
  @Nonnull
  public List<BodyInterceptor> getBodyInterceptors() {
    return bodyInterceptors;
  }

  @Nonnull
  public static PathBasedAnalysisInputLocation create(
      @Nonnull Path path, @Nonnull SourceType sourceType) {
    return PathBasedAnalysisInputLocation.create(path, sourceType, Collections.emptyList());
  }

  @Nonnull
  public static PathBasedAnalysisInputLocation create(
      @Nonnull Path path,
      @Nonnull SourceType srcType,
      @Nonnull List<BodyInterceptor> bodyInterceptors) {
    final PathBasedAnalysisInputLocation inputLocation;
    if (Files.isDirectory(path)) {
      inputLocation = new DirectoryBasedAnalysisInputLocation(path, srcType, bodyInterceptors);
    } else if (PathUtils.isArchive(path)) {
      if (PathUtils.hasExtension(path, FileType.JAR)) {
        inputLocation = new ArchiveBasedAnalysisInputLocation(path, srcType, bodyInterceptors);
      } else if (PathUtils.hasExtension(path, FileType.WAR)) {
        try {
          inputLocation = new WarArchiveAnalysisInputLocation(path, srcType, bodyInterceptors);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        throw new IllegalArgumentException(
            "Path '"
                + path.toAbsolutePath()
                + "' has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, war etc.");
      }
    } else if (PathUtils.hasExtension(path, FileType.CLASS)) {
      inputLocation = new ClassFileBasedAnalysisInputLocation(path, srcType, bodyInterceptors);
    } else {
      throw new IllegalArgumentException(
          "Path '"
              + path.toAbsolutePath()
              + "' has to be pointing to the root of a class container, e.g. directory, jar, zip, apk, war etc.");
    }
    return inputLocation;
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

      return Boolean.parseBoolean(value);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }

  @Nonnull
  Collection<JavaSootClassSource> walkDirectory(
      @Nonnull Path dirPath,
      @Nonnull IdentifierFactory factory,
      @Nonnull ClassProvider classProvider) {
    try {
      final FileType handledFileType = classProvider.getHandledFileType();
      final String moduleInfoFilename = JavaModuleIdentifierFactory.MODULE_INFO_FILE + ".class";
      return Files.walk(dirPath)
          .filter(
              filePath ->
                  PathUtils.hasExtension(filePath, handledFileType)
                      && !filePath.toString().endsWith(moduleInfoFilename))
          .flatMap(
              p ->
                  StreamUtils.optionalToStream(
                      classProvider.createClassSource(this, p, factory.fromPath(dirPath, p))))
          .map(src -> (JavaSootClassSource) src)
          .collect(Collectors.toList());

    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Nonnull
  protected Optional<JavaSootClassSource> getClassSourceInternal(
      @Nonnull JavaClassType signature, @Nonnull Path path, @Nonnull ClassProvider classProvider) {

    Path pathToClass =
        path.resolve(
            path.getFileSystem()
                .getPath(
                    signature.getFullyQualifiedName().replace('.', '/')
                        + classProvider.getHandledFileType().getExtensionWithDot()));
    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    Optional<? extends SootClassSource> classSource =
        classProvider.createClassSource(this, pathToClass, signature);

    return classSource.map(src -> (JavaSootClassSource) src);
  }

  protected Optional<JavaSootClassSource> getSingleClass(
      @Nonnull JavaClassType signature, @Nonnull Path path, @Nonnull ClassProvider classProvider) {

    Path pathToClass = Paths.get(path.toString());

    if (!Files.exists(pathToClass)) {
      return Optional.empty();
    }

    Optional<? extends SootClassSource> classSource =
        classProvider.createClassSource(this, pathToClass, signature);

    return classSource.map(src -> (JavaSootClassSource) src);
  }

  private static class ClassFileBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    public ClassFileBasedAnalysisInputLocation(
        @Nonnull Path classFilePath, @Nonnull SourceType srcType) {
      this(classFilePath, srcType, Collections.emptyList());
    }

    public ClassFileBasedAnalysisInputLocation(
        @Nonnull Path classFilePath,
        @Nonnull SourceType srcType,
        @Nonnull List<BodyInterceptor> bodyInterceptors) {
      super(classFilePath, srcType, bodyInterceptors);
    }

    @Override
    @Nonnull
    public Optional<JavaSootClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull View view) {
      return getSingleClass((JavaClassType) type, path, new AsmJavaClassProvider(view));
    }

    @Nonnull
    @Override
    public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
      AsmJavaClassProvider classProvider = new AsmJavaClassProvider(view);
      IdentifierFactory factory = view.getIdentifierFactory();
      Path dirPath = this.path.getParent();
      Optional<JavaSootClassSource> classSource =
          classProvider
              .createClassSource(this, path, factory.fromPath(dirPath, path))
              .map(src -> (JavaSootClassSource) src);
      return Collections.singletonList(classSource.get());
    }
  }

  private static class DirectoryBasedAnalysisInputLocation extends PathBasedAnalysisInputLocation {

    private DirectoryBasedAnalysisInputLocation(@Nonnull Path path, @Nonnull SourceType srcType) {
      this(path, srcType, Collections.emptyList());
    }

    private DirectoryBasedAnalysisInputLocation(
        @Nonnull Path path,
        @Nonnull SourceType srcType,
        @Nonnull List<BodyInterceptor> bodyInterceptors) {
      super(path, srcType, bodyInterceptors);
    }

    @Override
    @Nonnull
    public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {
      return walkDirectory(path, view.getIdentifierFactory(), new AsmJavaClassProvider(view));
    }

    @Override
    @Nonnull
    public Optional<JavaSootClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull View view) {
      return getClassSourceInternal((JavaClassType) type, path, new AsmJavaClassProvider(view));
    }
  }

  private static final class WarArchiveAnalysisInputLocation
      extends DirectoryBasedAnalysisInputLocation {
    public List<AnalysisInputLocation> containedInputLocations = new ArrayList<>();
    public static int maxAllowedBytesToExtract =
        1024 * 1024 * 500; // limit of extracted file size to protect against archive bombs

    private WarArchiveAnalysisInputLocation(@Nonnull Path warPath, @Nonnull SourceType srcType)
        throws IOException {
      this(warPath, srcType, Collections.emptyList());
    }

    private WarArchiveAnalysisInputLocation(
        @Nonnull Path warPath,
        @Nonnull SourceType srcType,
        @Nonnull List<BodyInterceptor> bodyInterceptors)
        throws IOException {
      super(
          Files.createTempDirectory("sootUp-war-" + warPath.hashCode()).toAbsolutePath(),
          srcType,
          bodyInterceptors);

      extractWarFile(warPath, path);

      Path webInfPath = path.resolve("WEB-INF");
      // directorystructre as specified in SRV.9.5 of
      // https://download.oracle.com/otn-pub/jcp/servlet-2.4-fr-spec-oth-JSpec/servlet-2_4-fr-spec.pdf?AuthParam=1625059899_16c705c72f7db7f85a8a7926558701fe
      Path classDir = webInfPath.resolve("classes");
      if (Files.exists(classDir)) {
        containedInputLocations.add(
            new DirectoryBasedAnalysisInputLocation(classDir, srcType, bodyInterceptors));
      }

      Path libDir = webInfPath.resolve("lib");
      if (Files.exists(libDir)) {
        try {
          Files.walk(libDir)
              .filter(f -> PathUtils.hasExtension(f, FileType.JAR))
              .forEach(
                  f ->
                      containedInputLocations.add(
                          new ArchiveBasedAnalysisInputLocation(f, srcType, bodyInterceptors)));
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }

    @Override
    @Nonnull
    public Collection<JavaSootClassSource> getClassSources(@Nonnull View view) {

      Set<SootClassSource> foundClasses = new HashSet<>();

      for (AnalysisInputLocation inputLoc : containedInputLocations) {
        foundClasses.addAll(inputLoc.getClassSources(view));
      }
      return foundClasses.stream()
          .map(src -> (JavaSootClassSource) src)
          .collect(Collectors.toList());
    }

    @Override
    @Nonnull
    public Optional<JavaSootClassSource> getClassSource(
        @Nonnull ClassType type, @Nonnull View view) {

      for (AnalysisInputLocation inputLocation : containedInputLocations) {
        final Optional<? extends SootClassSource> classSource =
            inputLocation.getClassSource(type, view);
        if (classSource.isPresent()) {
          return classSource.map(src -> (JavaSootClassSource) src);
        }
      }

      return Optional.empty();
    }

    /**
     * Extracts the war file at the temporary location to analyze underlying class and jar files
     *
     * <p>[ms] hint: extracting is necessary to access nested (zip)filesystems with java8/java9
     * runtime - nested (zip)filesystems would work with java11 runtime (maybe java10)
     *
     * @param warFilePath The path to war file to be extracted
     */
    protected void extractWarFile(Path warFilePath, final Path destDirectory) {
      int extractedSize = 0;
      try {
        File dest = destDirectory.toFile();
        if (!dest.exists()) {
          if (!dest.mkdir()) {
            throw new RuntimeException(
                "Could not create the directory to extract Warfile: " + destDirectory);
          }
          dest.deleteOnExit();
        }

        ZipInputStream zis =
            new ZipInputStream(Files.newInputStream(Paths.get(warFilePath.toString())));
        ZipEntry zipEntry;
        while ((zipEntry = zis.getNextEntry()) != null) {
          Path filepath = destDirectory.resolve(zipEntry.getName());
          final File file = filepath.toFile();

          file.deleteOnExit();
          if (zipEntry.isDirectory()) {
            file.mkdir();
          } else {
            byte[] incomingValues = new byte[4096];
            int readBytesZip;
            if (file.exists()) {
              // compare contents -> does it contain the extracted war already?
              int readBytesExistingFile;
              final BufferedInputStream bis =
                  new BufferedInputStream(Files.newInputStream(file.toPath()));
              byte[] bisBuf = new byte[4096];
              while ((readBytesZip = zis.read(incomingValues)) != -1) {
                if (extractedSize > maxAllowedBytesToExtract) {
                  throw new RuntimeException(
                      "The extracted warfile exceeds the size of "
                          + maxAllowedBytesToExtract
                          + " byte. Either the file is a big archive (-> increase PathBasedAnalysisInputLocation.WarArchiveInputLocation.maxAllowedBytesToExtract) or maybe it contains an archive bomb.");
                }
                readBytesExistingFile = bis.read(bisBuf, 0, readBytesZip);
                if (readBytesExistingFile != readBytesZip) {
                  throw new RuntimeException(
                      "Can't extract File \""
                          + file
                          + "\" as it already exists and has a different size.");
                } else if (!Arrays.equals(bisBuf, incomingValues)) {
                  throw new RuntimeException(
                      "Can't extract File \""
                          + file
                          + "\" as it already exists and has a different content which we can't override.");
                }
                extractedSize += readBytesZip;
              }

            } else {
              BufferedOutputStream bos =
                  new BufferedOutputStream(Files.newOutputStream(file.toPath()));
              while ((readBytesZip = zis.read(incomingValues)) != -1) {
                if (extractedSize > maxAllowedBytesToExtract) {
                  throw new RuntimeException(
                      "The extracted warfile exceeds the size of "
                          + maxAllowedBytesToExtract
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
