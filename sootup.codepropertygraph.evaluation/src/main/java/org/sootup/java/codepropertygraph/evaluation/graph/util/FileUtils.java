package org.sootup.java.codepropertygraph.evaluation.graph.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileUtils {
  public static void createDirectoryIfNotExists(Path dirPath) throws IOException {
    if (!Files.exists(dirPath)) {
      Files.createDirectories(dirPath);
    }
  }

  public static void deleteJsonFilesInDirectory(Path dirPath) throws IOException {
    try (Stream<Path> paths = Files.walk(dirPath)) {
      paths
          .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
          .filter(p -> p.toString().endsWith(".json"))
          .forEach(
              p -> {
                try {
                  Files.delete(p);
                } catch (IOException e) {
                  System.err.println("Failed to delete file: " + p);
                  e.printStackTrace();
                }
              });
    }
  }

  public static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }
}
