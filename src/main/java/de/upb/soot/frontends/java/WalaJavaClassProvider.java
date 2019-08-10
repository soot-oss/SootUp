package de.upb.soot.frontends.java;

import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.inputlocation.FileType;
import de.upb.soot.inputlocation.JavaSourcePathAnalysisInputLocation;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;

/** @author Linghui Luo */
public class WalaJavaClassProvider implements ClassProvider {

  @Override
  public ClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, JavaClassType type) {
    String exclusionFilePath =
        (srcNamespace instanceof JavaSourcePathAnalysisInputLocation)
            ? ((JavaSourcePathAnalysisInputLocation) srcNamespace).getExclusionFilePath()
            : null;
    return new WalaClassLoader(sourcePath.toString(), exclusionFilePath)
        .getClassSource(type)
        .orElseThrow(() -> new ResolveException("Could not resolve " + type + " in " + sourcePath));
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JAVA;
  }
}
