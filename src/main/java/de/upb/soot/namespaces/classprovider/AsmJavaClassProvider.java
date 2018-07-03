package de.upb.soot.namespaces.classprovider;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;

public class AsmJavaClassProvider implements IClassProvider {

  @Override
  public SootClass getSootClass(ClassSource classSource) {
    return null;
  }

  @Override
  public FileType getHandledFileType() {
    return FileType.CLASS;
  }
}
