package sootup.java.bytecode.frontend.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2024 Markus Schmidt and others
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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sootup.core.frontend.SootClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.FileType;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.util.PathUtils;
import sootup.core.views.View;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.core.JavaSootClassSource;

final class WarArchiveAnalysisInputLocation extends DirectoryBasedAnalysisInputLocation {
  public List<AnalysisInputLocation> containedInputLocations = new ArrayList<>();
  public static int maxAllowedBytesToExtract =
      1024 * 1024 * 500; // limit of extracted file size to protect against archive bombs

  private WarArchiveAnalysisInputLocation(@NonNull Path warPath, @NonNull SourceType srcType)
      throws IOException {
    this(
        warPath,
        srcType,
        BytecodeBodyInterceptors.Default.getBodyInterceptors(),
        Collections.emptyList());
  }

  WarArchiveAnalysisInputLocation(
      @NonNull Path warPath,
      @NonNull SourceType srcType,
      @NonNull List<BodyInterceptor> bodyInterceptors,
      @NonNull Collection<Path> ignoredPaths)
      throws IOException {
    super(
        Files.createTempDirectory("sootUp-war-" + warPath.hashCode()).toAbsolutePath(),
        srcType,
        bodyInterceptors,
        ignoredPaths);

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
      try (Stream<Path> paths = Files.walk(libDir)) {
        paths
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

  public WarArchiveAnalysisInputLocation(
      Path path, SourceType srcType, List<BodyInterceptor> bodyInterceptors) throws IOException {
    this(path, srcType, bodyInterceptors, Collections.emptyList());
  }

  @Override
  @NonNull
  public Stream<JavaSootClassSource> getClassSources(@NonNull View view) {
    return containedInputLocations.stream()
        .flatMap(location -> location.getClassSources(view))
        .map(src -> (JavaSootClassSource) src);
  }

  @Override
  @NonNull
  public Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type, @NonNull View view) {

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
  void extractWarFile(Path warFilePath, final Path destDirectory) {
    int extractedSize = 0;
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(warFilePath))) {
      File dest = destDirectory.toFile();
      if (!dest.exists()) {
        if (!dest.mkdir()) {
          throw new RuntimeException(
              "Could not create the directory to extract Warfile: " + destDirectory);
        }
        dest.deleteOnExit();
      }

      ZipEntry zipEntry;
      while ((zipEntry = zis.getNextEntry()) != null) {
        Path filepath = destDirectory.resolve(zipEntry.getName());
        final File file = filepath.toFile();

        String canonicalFilepathStr = file.getCanonicalPath();
        String canonicalDestDirStr = dest.getCanonicalPath();
        if (!canonicalFilepathStr.startsWith(canonicalDestDirStr + File.separator)) {
          throw new IllegalArgumentException(
              "ZipSlip Attack Mitigated: ZipEntry points outside of the target dir: "
                  + file.getName());
        }

        file.deleteOnExit();
        if (zipEntry.isDirectory()) {
          boolean mkdir = file.mkdir();
          if (!mkdir) {
            throw new IllegalStateException(
                "Could not create Directory: " + file.getAbsolutePath());
          }
        } else {
          byte[] incomingValues = new byte[4096];
          int readBytesZip;
          if (file.exists()) {
            // compare contents -> does it contain the extracted war already?
            int readBytesExistingFile;
            try (InputStream fis = Files.newInputStream(file.toPath());
                final BufferedInputStream bis = new BufferedInputStream(fis)) {
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
            }

          } else {
            try (OutputStream fos = Files.newOutputStream(file.toPath());
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
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
            }
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
   * @param extractedWARPath The path where the war file is extracted Adds the classes associated to
   *     servlet-class in a {@link ArrayList} of {@link String}
   */
  @NonNull
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
          classesInXML.add(eElement.getElementsByTagName("servlet-class").item(0).getTextContent());
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      throw new RuntimeException(e);
    }
    return classesInXML;
  }
}
