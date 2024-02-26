package org.sootup.java.codepropertygraph.evaluation;

import io.joern.jimple2cpg.Config;
import io.joern.jimple2cpg.Jimple2Cpg;
import io.shiftleft.codepropertygraph.generated.Cpg;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import scala.Option;
import scala.collection.immutable.Seq;
import scala.jdk.CollectionConverters;
import scala.util.Try;

public class JoernArtifactGenerator {

  public static void main(String[] args) throws IOException {
    String resourcesDir = "sootup.codepropertygraph.evaluation/src/test/resources/";
    Path inputDir = Paths.get(resourcesDir + "sootup-artifacts/");
    String outputDir = resourcesDir + "joern-artifacts/";

    List<File> jarFiles = listJarFiles(inputDir);
    for (File jarFile : jarFiles) {
      String inputPath = jarFile.getCanonicalPath();

      String jarNameWithoutExtension = jarFile.getName().replaceFirst("[.][^.]+$", "");
      String outputPath = Paths.get(outputDir, jarNameWithoutExtension + ".bin").toString();

      createCpg(inputPath, outputPath);
    }
  }

  public static void createCpg(String jarFilePath, String outputDirectory) {
    Seq<String> dynamicDirs =
        CollectionConverters.ListHasAsScala(Collections.singletonList(jarFilePath))
            .asScala()
            .toSeq();

    Config config =
        new Config(
            Option.empty(),
            dynamicDirs,
            CollectionConverters.ListHasAsScala(new ArrayList<String>()).asScala().toSeq(),
            false);
    config = (Config) config.withInputPath(jarFilePath);
    config = (Config) config.withOutputPath(outputDirectory);

    Try<Cpg> cpgTry = new Jimple2Cpg().createCpg(config);

    if (cpgTry.isSuccess()) {
      Cpg cpg = cpgTry.get();
      cpg.close();
    } else if (cpgTry.isFailure()) {
      Throwable exception = cpgTry.failed().get();
      throw new RuntimeException(exception.getMessage());
    }
  }

  public static List<File> listJarFiles(Path dir) {
    File[] files = dir.toFile().listFiles(((dir1, name) -> name.endsWith(".jar")));
    List<File> jarFiles = new ArrayList<>();
    if (files != null) {
      Collections.addAll(jarFiles, files);
    }
    return jarFiles;
  }
}
