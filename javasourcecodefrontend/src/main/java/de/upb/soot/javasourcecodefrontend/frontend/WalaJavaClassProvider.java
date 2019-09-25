package de.upb.soot.javasourcecodefrontend.frontend;

import de.upb.soot.frontends.ClassProvider;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.inputlocation.FileType;
import de.upb.soot.types.JavaClassType;
import java.nio.file.Path;
import javax.annotation.Nullable;

/** @author Linghui Luo */
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
      AnalysisInputLocation srcNamespace, Path sourcePath, JavaClassType type) {
    return new WalaClassLoader(sourcePath.toString(), exclusionFilePath)
        .getClassSource(type)
        .orElseThrow(() -> new ResolveException("Could not resolve " + type + " in " + sourcePath));
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JAVA;
  }
}
