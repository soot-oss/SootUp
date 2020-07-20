package de.upb.swt.soot.java.bytecode.inputlocation;

import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.util.PathUtils;
import de.upb.swt.soot.core.util.StreamUtils;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

/**
 * The class handles the operations for handling the operations for the {@link
 * AnalysisInputLocation} for the JAR and WAR files. It handles directories, archives (including
 * wildcard denoted archives) as stated in the official documentation:
 * https://docs.oracle.com/javase/8/docs/technotes/tools/windows/classpath.html
 *
 * @author Kaustubh Kelkar created on 20.07.2020
 */
public class FileHandling {

  protected Path path;
  public static List<Path> jarsFromPath = new ArrayList<>();
  public static List<String> classesInXML = new ArrayList<>();

  public FileHandling(@Nonnull String path) {
    this.path = Paths.get(path);
  }

  public @Nonnull List<Path> getJarsFromPath() {

    try {
      jarsFromPath = this.walkDirectoryForJars(Paths.get(extractWarFile(path.toString())));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return jarsFromPath;
  }

  /**
   * Return the list of the JAR files at the directory
   *
   * @param dirPath is the path for the extracted directory for the WAR file
   */
  @Nonnull
  List<Path> walkDirectoryForJars(@Nonnull Path dirPath) throws IOException {
    return Files.walk(dirPath)
        .filter(filePath -> PathUtils.hasExtension(filePath, FileType.JAR))
        .flatMap(p1 -> StreamUtils.optionalToStream(Optional.of(p1)))
        .collect(Collectors.toList());
  }

  static @Nonnull String extractWarFile(String warFilePath) {
    // FIXME: [ms] protect against archive bombs
    String destDirectory =
        System.getProperty("java.io.tmpdir")
            + File.separator
            + "sootOutput"
            + "-war-"
            + warFilePath.hashCode();

    try {
      File dest = new File(destDirectory);
      dest.deleteOnExit();
      if (!dest.exists()) {
        dest.mkdir();
      }
      ZipInputStream zis = new ZipInputStream(new FileInputStream(warFilePath));
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
    parseWebxml(destDirectory);
    return destDirectory;
  }

  /**
   * Parses the web.xml file to search for the servlet-class classes in the extracted directory
   * after the war file is extracted
   *
   * @param extractedWARPath The path where the war file is extracted Adds the classes associated to
   *     servlet-class in a {@link ArrayList} of {@link String}
   */
  private static void parseWebxml(String extractedWARPath) {
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
          classesInXML.add(eElement.getElementsByTagName("servlet-class").item(0).getTextContent());
        }
      }
    } catch (ParserConfigurationException | SAXException | IOException e) {
      e.getMessage();
    }
  }
}
