package org.sootup.java.codepropertygraph.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import soot.G;
import soot.PackManager;
import soot.Scene;
import soot.options.Options;

public class SootArtifactGenerator {

  public static void main(String[] args) throws IOException {
    String resourcesDir = "sootup.codepropertygraph.evaluation/src/test/resources/";
    Path inputDir = Paths.get(resourcesDir + "sootup-artifacts/");
    String outputBasePath = inputDir.toFile().getCanonicalPath();

    List<File> jarFiles = listJarFiles(inputDir);
    for (File jarFile : jarFiles) {
      String inputPath = jarFile.getCanonicalPath();

      String jarNameWithoutExtension = jarFile.getName().replaceFirst("[.][^.]+$", "");
      String outputPath = Paths.get(outputBasePath, jarNameWithoutExtension).toString();

      configureSoot(inputPath, outputPath);
      loadClassesAndGenerateJimple(inputPath);

      G.reset();
    }
  }

  private static void configureSoot(String inputPath, String outputPath) {
    G.reset();

    Options.v()
        .set_soot_classpath(inputPath + File.pathSeparator + System.getProperty("java.class.path"));

    Options.v().set_app(false);
    Options.v().set_whole_program(true);
    Options.v().set_keep_line_number(true);
    Options.v().set_keep_offset(true);
    Options.v().set_no_bodies_for_excluded(true);
    Options.v().set_allow_phantom_refs(true);
    Options.v().set_output_format(Options.output_format_jimple);
    Options.v().set_output_dir(outputPath);

    Options.v().setPhaseOption("jb", "use-original-names:false");
    Options.v().set_prepend_classpath(true);
    Options.v().setPhaseOption("jb.sils", "enabled:false");
    Options.v().setPhaseOption("jb", "use-original-names:true");
    Options.v().set_show_exception_dests(true);
    Options.v().set_omit_excepting_unit_edges(false);
  }

  private static void loadClassesAndGenerateJimple(String inputJar) {
    Options.v().set_process_dir(Collections.singletonList(inputJar));
    Scene.v().loadNecessaryClasses();

    PackManager.v().runPacks();
    PackManager.v().writeOutput();
  }

  public static List<File> listJarFiles(Path dir) {
    File[] files = dir.toFile().listFiles((dir1, name) -> name.endsWith(".jar"));
    List<File> jarFiles = new ArrayList<>();
    if (files != null) {
      Collections.addAll(jarFiles, files);
    }
    return jarFiles;
  }
}
