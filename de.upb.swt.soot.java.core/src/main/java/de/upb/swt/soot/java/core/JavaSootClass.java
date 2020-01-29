package de.upb.swt.soot.java.core;

import com.google.common.base.Suppliers;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SourceType;
import java.util.function.Supplier;

public class JavaSootClass extends SootClass {

  public boolean isJavaLibraryClass() {
    return this.classSignature.isBuiltInClass();
  }

  public JavaSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
  }

  private final Supplier<Iterable<AnnotationType>> lazyAnnotations =
      Suppliers.memoize(((JavaSootClassSource) classSource)::resolveAnnotations);

  Iterable<AnnotationType> getAnnotations() {
    return lazyAnnotations.get();
  }
}
