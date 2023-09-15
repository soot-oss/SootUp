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

package qilin;

import soot.SourceLocator;

import java.util.List;

public class CoreConfig {
    protected static CoreConfig coreConfig = null;

    public static CoreConfig v() {
        if (coreConfig == null) {
            throw new RuntimeException("Core configuration is not initialized!");
        }
        return coreConfig;
    }

    public static void reset() {
        coreConfig = null;
    }

    public enum ClinitMode {
        FULL, ONFLY, APP
    }

    public static class CorePTAConfiguration {
        public boolean singleentry = false;
        /**
         * Clinit loading mode:
         * APP: A debug mode for testing which load only the minimum <clinit> of classes needed.
         * FULL: load all <clinit> of classes that are currently loaded in the scene.
         * ON_THE_FLY: we load <clinit> on the fly, this mode is same as DOOP (default mode).
         */
        public ClinitMode clinitMode = ClinitMode.ONFLY;

        /**
         * enable to merge heaps like StringBuilder/StringBuffer, exceptions by types.
         */
        public boolean mergeHeap = false;

        /**
         * the type of array element is java.lang.Object by default.
         * if set to be true, we will use more precise type for array elements.
         */
        public boolean preciseArrayElement = false;

        /**
         * in qilin.spark propagate all string constants false means merge all string
         * constants, see the corresponding flag, DISTINGUISH_NO_STRING_CONSTANTS, in DOOP.
         */
        public boolean stringConstants = false;

        /**
         * The default setting (same as the imprecise version in Doop):
         * there is a global variable for all thrown exceptions and
         * any caught exception variable points to all those exceptions.
         * Obviously, this is extremely imprecise.
         * <p>
         * This imprecision does not seem to matter too much for typical
         * programs in the context-insensitive and 1-call-site-sensitive
         * analyses, but is disastrous in the 1-object-sensitive analysis.
         * <p>
         * once this switch is open, it will model exception flow-sensitively
         * (same as the precise version in Doop).
         * Implicitly thrown exceptions are not included.
         */
        public boolean preciseExceptions = false;

        /**
         * in qilin.spark limit heap context for strings if we are object sensitive
         */
        public boolean enforceEmptyCtxForIgnoreTypes = false;

        public String ptaName;
    }

    public static class ApplicationConfiguration {
        /**
         * Path for the root folder for the application classes or for the application
         * jar file.
         */
        public String APP_PATH = ".";
        /**
         * Path for the JRE to be used for whole program analysis.
         */
        public String JRE = null;
        /**
         * Path for the root folder for the library jars.
         */
        public String LIB_PATH = null;

        /**
         * Path for the reflection log file for the application.
         */
        public String REFLECTION_LOG = null;

        /**
         * Main class for the application.
         */
        public String MAIN_CLASS = null;

        /**
         * include selected packages which are not analyzed by default
         */
        public List<String> INCLUDE = null;
        /**
         * exclude selected packages
         */
        public List<String> EXCLUDE = null;
        /**
         * include packages which are not analyzed by default
         */
        public boolean INCLUDE_ALL = false;
    }

    public static class OutputConfiguration {
        public String outDir = SourceLocator.v().getOutputDir();
        /**
         * dump appclasses to jimple
         */
        public boolean dumpJimple = false;
        /**
         * if true, dump pts in app code to a file
         */
        public boolean dumppts = false;
        /**
         * if true, dump pts of vars in library
         */
        public boolean dumplibpts = false;
        /**
         * print a CG graph
         */
        public boolean dumpCallGraph = false;
        /**
         * print a PAG graph
         */
        public boolean dumppag = false;

        /**
         * if true, dump stats into files.
         */
        public boolean dumpStats = false;
    }

    protected CorePTAConfiguration ptaConfig;
    protected final ApplicationConfiguration appConfig = new ApplicationConfiguration();
    protected final OutputConfiguration outConfig = new OutputConfiguration();

    public CorePTAConfiguration getPtaConfig() {
        return ptaConfig;
    }

    public ApplicationConfiguration getAppConfig() {
        return appConfig;
    }

    public OutputConfiguration getOutConfig() {
        return outConfig;
    }

}
