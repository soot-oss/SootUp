package org.sootup.java.codepropertygraph.evaluation.joern;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class JoernCfgGenerator {

  public void generateCFG(String jarFilePath, String outputDirectory) {
    try {
      String cpgFilePath = "sootup.codepropertygraph.evaluation/src/main/temp/out.cpg";
      // Step 1: Parse the source code
      executeCommand(
          new String[] {
            "joern-parse.bat", "--output", cpgFilePath, jarFilePath, "--language", "java"
          });

      // Step 2: Export the CFG as a dot file
      executeCommand(
          new String[] {
            "joern-export.bat",
            "--format",
            "dot",
            "--out",
            outputDirectory,
            cpgFilePath,
            "--repr",
            "cfg"
          });

    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private void executeCommand(String[] command) throws IOException, InterruptedException {
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    Process process = processBuilder.start();

    // Reading the output of the command
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(process.getInputStream()))) {
      String line;
      while ((line = reader.readLine()) != null) {
        System.out.println(line);
      }
    }

    // Wait for the process to finish
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      System.out.println("Process exited with error code: " + exitCode);
    }
  }
}
