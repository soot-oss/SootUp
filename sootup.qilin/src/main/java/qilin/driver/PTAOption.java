/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.driver;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qilin.core.config.PointerAnalysisConfig;
import qilin.pta.toolkits.turner.Turner;

/**
 * Thin CLI translation layer: parses command-line arguments and builds a {@link PTAPattern} (to
 * drive {@link PTAFactory}) plus a type-safe {@link PointerAnalysisConfig} - it no longer mutates
 * any global/singleton configuration object.
 */
public class PTAOption extends Options {
  private static final Logger logger = LoggerFactory.getLogger(PTAOption.class);

  private String appPath = ".";
  private String libPath = null;
  private String jrePath = null;
  private String mainClass = null;
  private PTAPattern ptaPattern = new PTAPattern("insens");
  private final PointerAnalysisConfig.Builder configBuilder = PointerAnalysisConfig.builder();
  private boolean dumpJimpleFlag = false;

  /** add option "-brief -option" with description */
  protected void addOption(String brief, String option, String description) {
    addOption(new Option(brief, option, false, description));
  }

  /** add option "-brief -option &lt;arg&gt;" with description */
  protected void addOption(String brief, String option, String arg, String description) {
    addOption(
        OptionBuilder.withLongOpt(option)
            .withArgName(arg)
            .hasArg()
            .withDescription(description)
            .create(brief));
  }

  public PTAOption() {
    // input configurations
    addOption(
        "app",
        "apppath",
        "dir or jar",
        "The directory containing the classes for the application or the application jar file (default: .)");
    addOption(
        "lib",
        "libpath",
        "dir or jar",
        "The directory containing the library jar files for the application or the library jar file");
    addOption(
        null,
        "jre",
        "dir",
        "The directory containing the version of JRE to be used for whole program analysis");
    addOption(
        "reflog",
        "reflectionlog",
        "file",
        "The reflection log file for the application for resolving reflective call sites");
    addOption(
        "main",
        "mainclass",
        "class name",
        "Name of the main class for the application (must be specified when appmode)");

    // output configurations.
    addOption("jimple", "dumpjimple", "Dump appclasses to jimple. (default value: false)");
    addOption("stats", "dumpstats", "Dump statistics into files. (default value: false)");
    addOption(
        "ptsall",
        "dumpallpts",
        "Dump points-to of lib vars results to output/pts.txt (default value: false)");
    addOption("pts", "dumppts", "Dump points-to results to output/pts.txt (default value: false)");

    // general PTA configurations
    addOption(
        "clinit", "clinitmode", "APP|FULL|ONFLY", "clinit methods loading mode, default: ONFLY");
    addOption(
        "mh",
        "mergeheap",
        "merge heaps of StringBuilder/StringBuffer/Throwable (default value: false)");
    addOption(
        "lcs",
        "emptycontextforignoretypes",
        "Limit heap context to 0 for Strings/Exceptions in PTA (default value: false)");
    addOption(
        "pta",
        "pointstoanalysis",
        "<k>(c|o)+?(<h>h)?|insens",
        "Specify Pointer Analysis e.g. 2o1h or 2o -> 2obj+1heap (default value: insens; default h: k-1.)");
    addOption(
        "se",
        "singleentry",
        "A lightweight mode with only one main method entry. (default value: false)");
    addOption("sc", "stringconstants", "Propagate all string constants (default value: false)");
    addOption("pae", "precisearray", "Enable precise Array Element type (default value: false)");
    addOption(
        "pe", "preciseexceptions", "Enable precisely handling exceptions (default value: false)");

    // a specific PTA's configuration
    addOption(
        "tc",
        "turnerconfig",
        "[DEFAULT, PHASE_ONE, PHASE_TWO]",
        "Run Turner in the given setting (default value: DEFAULT)");
    addOption("cd", "ctxdebloat", "Enable context debloating optimization (default value: false)");
    addOption(
        "cda",
        "debloatapproach",
        "[CONCH, DEBLOATERX]",
        "Specify debloating approach (default value: CONCH)");
    addOption("tmd", "modular", "Enable Turner to run modularly (default value: false)");

    // others
    addOption("h", "help", "print this message");
    addOption("pre", "preonly", "Run only pre-analysis (default value: false)");
  }

  public void parseCommandLine(String[] args) {
    try {
      CommandLine cmd = new GnuParser().parse(this, args);
      if (cmd.hasOption("help")) {
        new HelpFormatter().printHelp("qilin", this);
        System.exit(0);
      }
      parseCommandLineOptions(cmd);
    } catch (Exception e) {
      logger.error("Error parsing command line options", e);
      System.exit(1);
    }
  }

  /**
   * Set all variables from the command line arguments.
   *
   * @param cmd
   */
  protected void parseCommandLineOptions(CommandLine cmd) {
    // pointer analysis configuration
    if (cmd.hasOption("apppath")) {
      appPath = cmd.getOptionValue("apppath");
    }
    String ptacmd = cmd.hasOption("pta") ? cmd.getOptionValue("pta") : "insens";
    ptaPattern = new PTAPattern(ptacmd);
    configBuilder.analysisName(ptaPattern.toString());
    if (cmd.hasOption("singleentry")) {
      configBuilder.singleEntry(true);
    }
    if (cmd.hasOption("mergeheap")) {
      configBuilder.heapAbstractionPolicy(PointerAnalysisConfig.HeapAbstractionPolicy.HEURISTIC_MERGE);
    }
    if (cmd.hasOption("stringconstants")) {
      configBuilder.stringConstants(true);
    }
    if (cmd.hasOption("emptycontextforignoretypes")) {
      configBuilder.enforceEmptyCtxForIgnoreTypes(true);
    }
    if (cmd.hasOption("clinitmode")) {
      configBuilder.clinitMode(
          PointerAnalysisConfig.ClinitMode.valueOf(cmd.getOptionValue("clinitmode")));
    }
    if (cmd.hasOption("preonly")) {
      configBuilder.preAnalysisOnly(true);
    }
    if (cmd.hasOption("ctxdebloat")) {
      configBuilder.ctxDebloating(true);
      if (cmd.hasOption("debloatapproach")) {
        configBuilder.debloatApproach(
            PointerAnalysisConfig.DebloatApproach.valueOf(cmd.getOptionValue("debloatapproach")));
      }
    }
    if (cmd.hasOption("preciseexceptions")) {
      configBuilder.preciseExceptions(true);
    }
    if (cmd.hasOption("modular")) {
      Turner.isModular = true;
    }
    if (cmd.hasOption("precisearray")) {
      configBuilder.preciseArrayElement(true);
    }
    // application configuration
    if (cmd.hasOption("mainclass")) {
      mainClass = cmd.getOptionValue("mainclass");
    }
    if (cmd.hasOption("jre")) {
      jrePath = cmd.getOptionValue("jre");
    }
    if (cmd.hasOption("libpath")) {
      libPath = cmd.getOptionValue("libpath");
    }
    if (cmd.hasOption("reflectionlog")) {
      configBuilder.reflectionLogPath(cmd.getOptionValue("reflectionlog"));
    }
    if (cmd.hasOption("turnerconfig")) {
      configBuilder.turnerConfig(
          PointerAnalysisConfig.TurnerConfig.valueOf(cmd.getOptionValue("turnerconfig")));
    }
    // output
    if (cmd.hasOption("dumpjimple")) {
      dumpJimpleFlag = true;
      configBuilder.dumpJimple(true);
    }
    if (cmd.hasOption("dumppts")) {
      configBuilder.dumpPointsToSet(true);
    }
    if (cmd.hasOption("dumpallpts")) {
      configBuilder.dumpPointsToSet(true);
      configBuilder.dumpLibraryPointsToSet(true);
    }
    if (cmd.hasOption("dumpstats")) {
      configBuilder.dumpStats(true);
    }
  }

  public String getAppPath() {
    return appPath;
  }

  public String getLibPath() {
    return libPath;
  }

  public String getJrePath() {
    return jrePath;
  }

  public String getMainClass() {
    return mainClass;
  }

  public boolean isDumpJimple() {
    return dumpJimpleFlag;
  }

  public PTAPattern getPtaPattern() {
    return ptaPattern;
  }

  public PointerAnalysisConfig getPointerAnalysisConfig() {
    return configBuilder.build();
  }
}
