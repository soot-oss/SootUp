package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.model.SourceType;
import java.util.Set;
import javax.annotation.Nonnull;

public class JavaAnnotationSootClass extends JavaSootClass {

  public JavaAnnotationSootClass(JavaSootClassSource classSource, SourceType sourceType) {
    super(classSource, sourceType);
    getMethods().forEach(JavaAnnotationSootMethod::getDefaultValue);
  }

  @Nonnull
  @Override
  public Set<JavaAnnotationSootMethod> getMethods() {
    return (Set<JavaAnnotationSootMethod>) super.getMethods();
  }
}
