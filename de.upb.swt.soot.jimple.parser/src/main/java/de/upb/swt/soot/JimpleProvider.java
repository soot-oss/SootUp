package de.upb.swt.soot;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ClassProvider;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.FileType;
import de.upb.swt.soot.core.types.ClassType;
import java.nio.file.Path;

public class JimpleProvider implements ClassProvider {

  @Override
  public AbstractClassSource createClassSource(
      AnalysisInputLocation srcNamespace, Path sourcePath, ClassType classSignature) {
    // TODO implement
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.JIMPLE;
  }
}
