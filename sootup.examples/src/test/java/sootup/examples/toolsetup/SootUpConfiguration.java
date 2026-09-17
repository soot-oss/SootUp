package sootup.examples.toolsetup;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jspecify.annotations.NonNull;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.Type;
import sootup.core.types.VoidType;
import sootup.interceptors.BytecodeBodyInterceptors;
import sootup.java.bytecode.frontend.inputlocation.DefaultRuntimeAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaModulePathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaModuleIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * Convenient setup of SootUp by mimicking the flags accepted by the {@code java} executable.
 *
 * <p>This class is adapted from the <a
 * href="https://github.com/secure-software-engineering/API_ASSIST">API_ASSIST</a> project. Instead
 * of documenting how to build an arbitrary CLI from scratch, we show the parser that is already
 * used in the wild — so you can copy it directly.
 *
 * <p>Supported flags (mirrors {@code java} / {@code javac}):
 *
 * <ul>
 *   <li>{@code --cp} / {@code --classpath} / {@code --class-path} — application classpath
 *   <li>{@code -p} / {@code --module-path} — module path
 *   <li>{@code -m} / {@code --module} — module entry point
 *   <li>{@code --jar} — executable JAR (reads {@code Main-Class} from the manifest)
 * </ul>
 *
 * The first non-option argument is treated as the main class name. All remaining arguments are
 * forwarded to the main method as {@code String[]}.
 */
// --8<-- [start:class]
public class SootUpConfiguration {

  @NonNull private JavaView view;
  @NonNull private MethodSignature entrypoint;
  @NonNull private List<String> arguments;

  private static final MethodSubSignature MAIN_SIGNATURE =
      new MethodSubSignature(
          "main",
          List.of(
              Type.createArrayType(new JavaClassType("String", new PackageName("java.lang")), 1)),
          VoidType.getInstance());

  // --8<-- [start:constructor]
  public SootUpConfiguration(@NonNull String... args) throws ParseException, IOException {
    // --8<-- [start:options]
    Options options = new Options();
    // Long-only options: Option.builder() without opt() gives a long-only flag (no -x short form).
    options.addOption(
        Option.builder()
            .longOpt("jar")
            .hasArg()
            .argName("file")
            .desc("Executable JAR; Main-Class is read from its manifest.")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("class-path")
            .hasArg()
            .argName("path")
            .desc(
                "Application classpath (directories or JARs, separated by '"
                    + java.io.File.pathSeparator
                    + "').")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("classpath")
            .hasArg()
            .argName("path")
            .desc("Application classpath (alias for --class-path).")
            .build());
    options.addOption(
        Option.builder()
            .longOpt("cp")
            .hasArg()
            .argName("path")
            .desc("Application classpath (short alias).")
            .build());
    // Options with both a short and a long name (mirrors java/javac flags).
    options.addOption(
        Option.builder("m")
            .longOpt("module")
            .hasArg()
            .argName("module")
            .desc("Module entry point, e.g. com.example/com.example.Main.")
            .build());
    options.addOption(
        Option.builder("p")
            .longOpt("module-path")
            .hasArg()
            .argName("path")
            .desc("Module path.")
            .build());
    // --8<-- [end:options]

    CommandLineParser parser = new DefaultParser(false);
    CommandLine cmd = parser.parse(options, args, true);

    List<AnalysisInputLocation> locations = new ArrayList<>();

    // --8<-- [start:classpath-handling]
    // Collect all --cp / --classpath / --class-path values (all three are accepted, just like
    // java).
    for (String opt : new String[] {"cp", "classpath", "class-path"}) {
      if (cmd.hasOption(opt)) {
        locations.add(
            new JavaClassPathAnalysisInputLocation(
                cmd.getOptionValue(opt),
                SourceType.Application,
                BytecodeBodyInterceptors.Default.getBodyInterceptors()));
      }
    }

    if (locations.isEmpty()) {
      // Fall back to the current working directory when nothing is specified.
      locations.add(
          new JavaClassPathAnalysisInputLocation(
              System.getProperty("user.dir"),
              SourceType.Application,
              BytecodeBodyInterceptors.Default.getBodyInterceptors()));
    }
    // --8<-- [end:classpath-handling]

    if (cmd.hasOption("module-path")) {
      locations.add(
          new JavaModulePathAnalysisInputLocation(
              java.nio.file.Paths.get(cmd.getOptionValue("module-path")),
              FileSystems.getDefault(),
              SourceType.Application,
              BytecodeBodyInterceptors.Default.getBodyInterceptors()));
    }

    // --8<-- [start:entrypoint]
    List<String> positional = cmd.getArgList();

    if (cmd.hasOption("jar")) {
      String jarFile = cmd.getOptionValue("jar");
      locations.add(
          new JavaClassPathAnalysisInputLocation(
              jarFile,
              SourceType.Application,
              BytecodeBodyInterceptors.Default.getBodyInterceptors()));

      // Read Main-Class from the JAR manifest — no need to specify it separately.
      try (JarInputStream jis = new JarInputStream(new FileInputStream(jarFile))) {
        Manifest manifest = jis.getManifest();
        Attributes attrs = manifest.getMainAttributes();
        String mainClass = attrs.getValue("Main-Class");
        this.entrypoint =
            new MethodSignature(
                JavaIdentifierFactory.getInstance().getClassType(mainClass), MAIN_SIGNATURE);
      }
    } else if (cmd.hasOption("module")) {
      this.entrypoint =
          new MethodSignature(
              JavaModuleIdentifierFactory.getInstance().getClassType(cmd.getOptionValue("module")),
              MAIN_SIGNATURE);
    } else {
      if (positional.isEmpty()) {
        throw new IllegalArgumentException("No main class specified and no --jar/--module given.");
      }
      // First positional argument is the main class; the rest are forwarded to main(String[]).
      String mainClass = positional.remove(0);
      this.entrypoint =
          new MethodSignature(
              JavaIdentifierFactory.getInstance().getClassType(mainClass), MAIN_SIGNATURE);
    }
    // --8<-- [end:entrypoint]

    // Always add the JRE as a library so that java.lang.* and friends resolve correctly.
    locations.add(
        new DefaultRuntimeAnalysisInputLocation(
            SourceType.Library, BytecodeBodyInterceptors.Default.getBodyInterceptors()));

    this.view = new JavaView(locations);
    this.arguments = positional;
  }

  // --8<-- [end:constructor]

  public @NonNull JavaView getView() {
    return view;
  }

  public @NonNull MethodSignature getEntrypoint() {
    return entrypoint;
  }

  public @NonNull List<String> getArguments() {
    return arguments;
  }
}
// --8<-- [end:class]
