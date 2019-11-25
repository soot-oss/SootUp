package de.upb.swt.soot.java.sourcecode.frontend;

import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;
import javax.annotation.Nullable;

/**
 * A {@link ClassProvider} that can read Java source code
 *
 * @author Linghui Luo
 */
public class WalaJavaClassProvider implements ClassProvider {

  @Nullable private final String exclusionFilePath;

  public WalaJavaClassProvider() {
    this(null);
  }

  public WalaJavaClassProvider(@Nullable String exclusionFilePath) {
    this.exclusionFilePath = exclusionFilePath;
  }

  @Override
  public ClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, ClassType type) {
    return new WalaClassLoader(sourcePath.toString(), exclusionFilePath)
        .getClassSource(type)
        .orElseThrow(() -> new ResolveException("Could not resolve " + type + " in " + sourcePath));
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JAVA;
  }
}
