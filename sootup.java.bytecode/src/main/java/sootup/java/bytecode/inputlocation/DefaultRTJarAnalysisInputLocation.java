package sootup.java.bytecode.inputlocation;

import java.nio.file.Paths;
import javax.annotation.Nonnull;
import sootup.core.model.SourceType;

/**
 * Refers to the rt.jar from <=Java8 as an AnalysisInputLocation requires: JAVA_HOME to be set and
 * expects the jar in the "lib/" subdirectory. If you need to include the rt.jar from a custom
 * Location please make use of JavaClassPathAnalysisInputLocation.
 */
public class DefaultRTJarAnalysisInputLocation
    extends PathBasedAnalysisInputLocation.ArchiveBasedAnalysisInputLocation {

  public DefaultRTJarAnalysisInputLocation() {
    this(SourceType.Library);
  }

  public DefaultRTJarAnalysisInputLocation(@Nonnull SourceType srcType) {
    super(Paths.get(System.getProperty("java.home") + "/lib/rt.jar"), srcType);
  }
}
