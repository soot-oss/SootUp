package sootup.java.bytecode.inputlocation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;

public class DownloadJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  private static final int BUFFER_SIZE = 1024;

  public DownloadJarAnalysisInputLocation(
      String downloadURL, List<BodyInterceptor> bodyInterceptors, Collection<Path> ignoredPaths) {
    super(
        downloadAndConstructPath(downloadURL), SourceType.Library, bodyInterceptors, ignoredPaths);
  }

  private static Path downloadAndConstructPath(String downloadURL) {
    HttpURLConnection connection;
    String tempDirPath = System.getProperty("java.io.tmpdir");
    String filename = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
    File file = new File(tempDirPath, filename);
    try {
      URL url = new URL(downloadURL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("HTTP request failed with response code " + responseCode);
      }
      try (InputStream inputStream = new BufferedInputStream(connection.getInputStream());
          OutputStream outputStream = Files.newOutputStream(Paths.get(file.getAbsolutePath()))) {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer, 0, BUFFER_SIZE)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return file.toPath();
  }
}
