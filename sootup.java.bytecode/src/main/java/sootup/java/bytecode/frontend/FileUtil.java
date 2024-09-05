package sootup.java.bytecode.frontend;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtil {

  private static Path tempDirectory;

  public static Path getTempDirectory() {
    try {
      return tempDirectory == null
          ? (tempDirectory = Files.createTempDirectory("tempDir"))
          : tempDirectory;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
