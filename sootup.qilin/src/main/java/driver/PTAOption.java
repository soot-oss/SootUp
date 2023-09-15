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

package driver;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import qilin.pta.PTAConfig;
import qilin.pta.toolkits.turner.Turner;
import qilin.pta.tools.DebloatedPTA;

import java.util.*;

public class PTAOption extends Options {
    private static final Logger logger = LoggerFactory.getLogger(PTAOption.class);

    /**
     * add option "-brief -option" with description
     */
    protected void addOption(String brief, String option, String description) {
        addOption(new Option(brief, option, false, description));
    }

    /**
     * add option "-brief -option <arg>" with description
     */
    protected void addOption(String brief, String option, String arg, String description) {
        addOption(OptionBuilder.withLongOpt(option).withArgName(arg).hasArg().withDescription(description).create(brief));
    }

    public PTAOption() {
        // input configurations
        addOption("app", "apppath", "dir or jar",
                "The directory containing the classes for the application or the application jar file (default: .)");
        addOption("lib", "libpath", "dir or jar",
                "The directory containing the library jar files for the application or the library jar file");
        addOption(null, "includeall", "Include packages which are not analyzed by default. (default value: false)");
        addOption(null, "exclude", "packages", "Exclude selected packages. (delimiting symbol: semicolon ';')");
        addOption(null, "jre", "dir",
                "The directory containing the version of JRE to be used for whole program analysis");
        addOption("reflog", "reflectionlog", "file",
                "The reflection log file for the application for resolving reflective call sites");
        addOption("main", "mainclass", "class name",
                "Name of the main class for the application (must be specified when appmode)");

        // output configurations.
        addOption("cg", "dumpcallgraph", "Output .dot callgraph file (default value: false)");
        addOption("jimple", "dumpjimple", "Dump appclasses to jimple. (default value: false)");
        addOption("stats", "dumpstats", "Dump statistics into files. (default value: false)");
        addOption("ptsall", "dumpallpts",
                "Dump points-to of lib vars results to output/pts.txt (default value: false)");
        addOption("pag", "dumppag", "Print PAG to terminal. (default value: false)");
        addOption("pts", "dumppts", "Dump points-to results to output/pts.txt (default value: false)");

        // general PTA configurations
        addOption("clinit", "clinitmode", "APP|FULL|ONFLY", "clinit methods loading mode, default: ONFLY");
        addOption("mh", "mergeheap",
                "merge heaps of StringBuilder/StringBuffer/Throwable (default value: false)");
        addOption("lcs", "emptycontextforignoretypes", "Limit heap context to 0 for Strings/Exceptions in PTA (default value: false)");
        addOption("pta", "pointstoanalysis", "<k>(c|o)+?(<h>h)?|insens",
                "Specify Pointer Analysis e.g. 2o1h or 2o -> 2obj+1heap (default value: insens; default h: k-1.)");
        addOption("se", "singleentry", "A lightweight mode with only one main method entry. (default value: false)");
        addOption("sc", "stringconstants", "Propagate all string constants (default value: false)");
        addOption("pae", "precisearray", "Enable precise Array Element type (default value: false)");
        addOption("pe", "preciseexceptions", "Enable precisely handling exceptions (default value: false)");

        // a specific PTA's configuration
        addOption("tc", "turnerconfig", "[DEFAULT, PHASE_ONE, PHASE_TWO]", "Run Turner in the given setting (default value: DEFAULT)");
        addOption("cd", "ctxdebloat", "Enable context debloating optimization (default value: false)");
        addOption("cda", "debloatapproach", "[CONCH, DEBLOATERX]", "Specify debloating approach (default value: CONCH)");
        addOption("tmd", "modular", "Enable Turner to run modularly (default value: false)");

        // callgraph algorithm configurations
        addOption("cga", "callgraphalg", "[CHA, VTA, RTA, SPARK, GEOM, QILIN]", "Specify callgraph construction algorithm (default value: QILIN)");

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
            PTAConfig.v().getAppConfig().APP_PATH = cmd.getOptionValue("apppath");
        }
        String ptacmd = cmd.hasOption("pta") ? cmd.getOptionValue("pta") : "insens";
        PTAConfig.v().getPtaConfig().ptaPattern = new PTAPattern(ptacmd);
        PTAConfig.v().getPtaConfig().ptaName = PTAConfig.v().getPtaConfig().ptaPattern.toString();
        if (cmd.hasOption("singleentry")) {
            PTAConfig.v().getPtaConfig().singleentry = true;
        }
        if (cmd.hasOption("mergeheap")) {
            PTAConfig.v().getPtaConfig().mergeHeap = true;
        }
        if (cmd.hasOption("stringconstants")) {
            PTAConfig.v().getPtaConfig().stringConstants = true;
        }
        if (cmd.hasOption("emptycontextforignoretypes")) {
            PTAConfig.v().getPtaConfig().enforceEmptyCtxForIgnoreTypes = true;
        }
        if (cmd.hasOption("clinitmode")) {
            PTAConfig.v().getPtaConfig().clinitMode = PTAConfig.ClinitMode.valueOf(cmd.getOptionValue("clinitmode"));
        }
        if (cmd.hasOption("preonly")) {
            PTAConfig.v().getPtaConfig().preAnalysisOnly = true;
        }
        if (cmd.hasOption("ctxdebloat")) {
            PTAConfig.v().getPtaConfig().ctxDebloating = true;
            if (cmd.hasOption("debloatapproach")) {
                PTAConfig.v().getPtaConfig().debloatApproach = DebloatedPTA.DebloatApproach.valueOf(cmd.getOptionValue("debloatapproach"));
            }
        }
        if (cmd.hasOption("preciseexceptions")) {
            PTAConfig.v().getPtaConfig().preciseExceptions = true;
        }
        if (cmd.hasOption("modular")) {
            Turner.isModular = true;
        }
        if (cmd.hasOption("precisearray")) {
            PTAConfig.v().getPtaConfig().preciseArrayElement = true;
        }
        // application configuration
        if (cmd.hasOption("mainclass")) {
            PTAConfig.v().getAppConfig().MAIN_CLASS = cmd.getOptionValue("mainclass");
        }
        if (cmd.hasOption("jre")) {
            PTAConfig.v().getAppConfig().JRE = cmd.getOptionValue("jre");
        }
        if (cmd.hasOption("libpath")) {
            PTAConfig.v().getAppConfig().LIB_PATH = cmd.getOptionValue("libpath");
        }
        if (cmd.hasOption("exclude")) {
            PTAConfig.v().getAppConfig().EXCLUDE = parsePackages(cmd.getOptionValue("exclude"));
        }
        if (cmd.hasOption("reflectionlog")) {
            PTAConfig.v().getAppConfig().REFLECTION_LOG = cmd.getOptionValue("reflectionlog");
        }
        if (cmd.hasOption("inlcudeall")) {
            PTAConfig.v().getAppConfig().INCLUDE_ALL = true;
        }
        if (cmd.hasOption("turnerconfig")) {
            PTAConfig.v().turnerConfig = PTAConfig.TurnerConfig.valueOf(cmd.getOptionValue("turnerconfig"));
        }
        // callgraph
        if (cmd.hasOption("callgraphalg")) {
            PTAConfig.v().callgraphAlg = PTAConfig.CallgraphAlgorithm.valueOf(cmd.getOptionValue("callgraphalg"));
        }
        // output
        if (cmd.hasOption("dumpjimple")) {
            PTAConfig.v().getOutConfig().dumpJimple = true;
        }
        if (cmd.hasOption("dumppts")) {
            PTAConfig.v().getOutConfig().dumppts = true;
        }
        if (cmd.hasOption("dumpallpts")) {
            PTAConfig.v().getOutConfig().dumppts = true;
            PTAConfig.v().getOutConfig().dumplibpts = true;
        }
        if (cmd.hasOption("dumpcallgraph")) {
            PTAConfig.v().getOutConfig().dumpCallGraph = true;
        }
        if (cmd.hasOption("dumppag")) {
            PTAConfig.v().getOutConfig().dumppag = true;
        }
        if (cmd.hasOption("dumpstats")) {
            PTAConfig.v().getOutConfig().dumpStats = true;
        }
    }

    static List<String> parsePackages(String packagesString) {
        ArrayList<String> pkgList = new ArrayList<>();
        String[] pkgs = packagesString.split(";");
        Collections.addAll(pkgList, pkgs);
        return pkgList;
    }
}
