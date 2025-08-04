package sootup.java.bytecode.frontend.inputlocation;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.List;
import sootup.core.model.SourceType;
import sootup.core.transform.BodyInterceptor;
import sootup.java.bytecode.frontend.FileUtil;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020
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

public class DownloadJarAnalysisInputLocation extends ArchiveBasedAnalysisInputLocation {

  public DownloadJarAnalysisInputLocation(
      String downloadURL, List<BodyInterceptor> bodyInterceptors, Collection<Path> ignoredPaths) {
    super(
        downloadAndConstructPath(downloadURL), SourceType.Library, bodyInterceptors, ignoredPaths);
  }

  private static Path downloadAndConstructPath(String downloadURL) {
    HttpURLConnection connection = null;
    String filename = downloadURL.substring(downloadURL.lastIndexOf("/") + 1);
    File file = new File(FileUtil.getTempDirectory().toString(), filename);
    try {
      URL url = new URL(downloadURL);
      connection = (HttpURLConnection) url.openConnection();
      connection.setRequestMethod("GET");
      int responseCode = connection.getResponseCode();
      if (responseCode != HttpURLConnection.HTTP_OK) {
        throw new IOException("HTTP request failed with response code " + responseCode);
      }
      try (InputStream inputStream = connection.getInputStream()) {
        Path outputPath = Paths.get(file.getAbsolutePath());
        Files.copy(inputStream, outputPath, StandardCopyOption.REPLACE_EXISTING);
      }
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    } finally {
      if (connection != null) {
        connection.disconnect();
      }
    }

    return file.toPath();
  }
}
