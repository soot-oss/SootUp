package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;

public class JavaSootClass extends SootClass {

  public boolean isJavaLibraryClass() {
    return this.classSignature.isBuiltInClass();
  }

  public JavaSootClass(SootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }
}
