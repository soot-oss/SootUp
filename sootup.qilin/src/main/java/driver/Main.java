package driver;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.model.SourceType;
import sootup.java.bytecode.inputlocation.BytecodeClassLoadingOptions;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Collection<String> getJreJars(String JRE) {
        if (JRE == null) {
            return Collections.emptySet();
        }
        final String jreLibDir = JRE + File.separator + "lib";
        return FileUtils.listFiles(new File(jreLibDir), new String[]{"jar"}, false).stream().map(File::toString)
                .collect(Collectors.toList());
    }

    /**
     * Returns a collection of files, one for each of the jar files in the app's lib
     * folder
     */
    private static Collection<String> getLibJars(String LIB_PATH) {
        if (LIB_PATH == null) {
            return Collections.emptySet();
        }
        File libFile = new File(LIB_PATH);
        if (libFile.exists()) {
            if (libFile.isDirectory()) {
                return FileUtils.listFiles(libFile, new String[]{"jar"}, true).stream().map(File::toString)
                        .collect(Collectors.toList());
            } else if (libFile.isFile()) {
                if (libFile.getName().endsWith(".jar")) {
                    return Collections.singletonList(LIB_PATH);
                }
                logger.error("Project not configured properly. Application library path {} is not a jar file.",
                        libFile);
                System.exit(1);
            }
        }
        logger.error("Project not configured properly. Application library path {} is not correct.", libFile);
        System.exit(1);
        return null;
    }

    public static void main(String[] args) {
        String appPath = "/Users/dongjie/Documents/Work/QilinOfficial/artifact/benchmarks/dacapo2006/luindex.jar";
        String libPath = "/Users/dongjie/Documents/Work/QilinOfficial/artifact/benchmarks/dacapo2006/luindex-deps.jar";
        String jrePath = "/Users/dongjie/Documents/Work/QilinOfficial/artifact/benchmarks/JREs/jre1.6.0_45";
        JavaProject.JavaProjectBuilder builder = new JavaProject.JavaProjectBuilder(new JavaLanguage(6));
        builder.addInputLocation(new JavaClassPathAnalysisInputLocation(appPath, SourceType.Application));
        for (String libJar : getLibJars(libPath)) {
            builder.addInputLocation(new JavaClassPathAnalysisInputLocation(libJar, SourceType.Library));
        }
        for (String jreJar : getJreJars(jrePath)) {
            builder.addInputLocation(new JavaClassPathAnalysisInputLocation(jreJar, SourceType.Library));
        }
        JavaProject project = builder.build();
        JavaView view = project.createOnDemandView(ail -> BytecodeClassLoadingOptions.Default);

    }
}
