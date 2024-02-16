package org.sootup.java.codepropertygraph.evaluation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.shiftleft.codepropertygraph.generated.nodes.Method;
import io.shiftleft.semanticcpg.dotgenerator.DotSerializer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.sootup.java.codepropertygraph.evaluation.joern.JoernDdgGenerator;
import org.sootup.java.codepropertygraph.evaluation.sootup.SootUpDdgGenerator;
import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.ddg.DdgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;

public class EvalDdg {
  private static final String JOERN_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/joern-artifacts";
  private static final String SOOTUP_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/sootup-artifacts";
  private static final String RESULT_DIR =
      "sootup.codepropertygraph.evaluation/src/test/resources/result-logs-ddg";
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
  private static final PrintStream originalSysOut = System.out;

  public static void main(String[] args) {
    try {
      Path resultDirPath = Paths.get(RESULT_DIR);
      createDirectoryIfNotExists(resultDirPath);
      deleteJsonFilesInDirectory(resultDirPath);

      List<Path> binFiles =
          Files.list(Paths.get(JOERN_DIR))
              .filter(p -> p.toString().endsWith(".bin"))
              .collect(Collectors.toList());

      // disableSysOut();
      setFileSysOut();

      for (Path binFile : binFiles) {
        String baseName = extractBaseName(binFile);
        Path targetDir = Paths.get(SOOTUP_DIR, baseName);

        if (!baseName.startsWith("lombok")) continue;

        if (Files.exists(targetDir) && Files.isDirectory(targetDir)) {
          processFilePair(binFile, targetDir);
        }
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static void processFilePair(Path binFile, Path targetDir) {
    System.out.println("Processing: " + targetDir);
    Map<String, Object> result = new HashMap<>();
    long startTime = System.currentTimeMillis();

    try {
      String cpgPath = binFile.toString();

      SootUpDdgGenerator sootUpDdgGenerator = new SootUpDdgGenerator(targetDir);
      JoernDdgGenerator joernDdgGenerator = new JoernDdgGenerator(cpgPath);
      DdgPropertyGraphComparer comparer =
          new DdgPropertyGraphComparer(joernDdgGenerator, sootUpDdgGenerator);

      for (SootMethod sootupMethod : sootUpDdgGenerator.getMethods()) {
        // System.out.println(sootUpDdgGenerator.getMethodSignatureAsJoern(sootupMethod));
        // if (true) continue;

        try {
          if (sootupMethod.isAbstract() || sootupMethod.isNative())
            continue; // Todo: handle abstract and native methods

          String methodSignatureAsJoern =
              sootUpDdgGenerator.getMethodSignatureAsJoern(sootupMethod);
          Optional<Method> joernMethodOpt = joernDdgGenerator.getMethod(methodSignatureAsJoern);

          if (!joernMethodOpt.isPresent()) continue;

          Method joernMethod = joernMethodOpt.get();

          DotSerializer.Graph joernDdg = joernDdgGenerator.getDdg(joernMethod);
          PropertyGraph sootUpDdg = DdgCreator.convert(new MethodInfo(sootupMethod));

          comparer.compareDdg(joernDdg, sootUpDdg, methodSignatureAsJoern);
        } catch (RuntimeException e) {
          e.printStackTrace();
        }
      }

      // System.out.println("The number of methods is: " + sootUpDdgGenerator.getMethods().size());

      int similarEdgesCount = comparer.getTotalSameEdges();
      int totalEdges = comparer.getTotalSameEdges() + comparer.getTotalDiffEdges();
      double similarityPercentage = ((double) similarEdgesCount / totalEdges) * 100;
      similarityPercentage = Math.round(similarityPercentage * 10000) / 10000.0;

      result.put("numOfMethods", comparer.getTotalMethods());
      result.put("differentEdges", comparer.getTotalDiffEdges());
      result.put("sameEdges", comparer.getTotalSameEdges());
      result.put("similarityPercentage", similarityPercentage + " %");

      result.put("failed", false);

    } catch (Exception e) {
      result.put("failed", true);
    } finally {
      long endTime = System.currentTimeMillis();
      long elapsedTime = endTime - startTime;

      // Break down the elapsed time
      long hours = elapsedTime / (60 * 60 * 1000);
      elapsedTime %= (60 * 60 * 1000);
      long minutes = elapsedTime / (60 * 1000);
      elapsedTime %= (60 * 1000);
      long seconds = elapsedTime / 1000;
      long milliseconds = elapsedTime % 1000;

      Map<String, Long> elapsedTimeDetails = new HashMap<>();
      elapsedTimeDetails.put("hours", hours);
      elapsedTimeDetails.put("minutes", minutes);
      elapsedTimeDetails.put("seconds", seconds);
      elapsedTimeDetails.put("milliseconds", milliseconds);

      result.put("elapsedTime", elapsedTimeDetails);
      result.put("failed", false);

      writeResultToFile(binFile, result);
    }
  }

  private static void writeResultToFile(Path binFile, Map<String, Object> result) {
    try {
      String baseName = extractBaseName(binFile);
      Path resultFilePath = Paths.get(RESULT_DIR, baseName + ".json");
      String json = GSON.toJson(result);
      Files.write(resultFilePath, json.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static String extractBaseName(Path path) {
    String fileName = path.getFileName().toString();
    return fileName.substring(0, fileName.lastIndexOf('.'));
  }

  private static void createDirectoryIfNotExists(Path dirPath) throws IOException {
    if (!Files.exists(dirPath)) {
      Files.createDirectories(dirPath);
    }
  }

  private static void disableSysOut() {
    System.setOut(
        new PrintStream(
            new OutputStream() {
              public void write(int b) {}
            }));
  }

  private static void setFileSysOut() {
    try {
      System.setOut(
          new PrintStream("sootup.codepropertygraph.evaluation/src/test/resources/main_out_ddg.txt"));
    } catch (FileNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private static void reenableSysOut() {
    System.setOut(originalSysOut);
  }

  private static void deleteJsonFilesInDirectory(Path dirPath) throws IOException {
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
}
