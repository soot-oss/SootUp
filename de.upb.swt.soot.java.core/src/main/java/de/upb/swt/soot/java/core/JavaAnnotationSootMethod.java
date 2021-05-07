package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.BodySource;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class JavaAnnotationSootMethod extends JavaSootMethod {

  public JavaAnnotationSootMethod(
      @Nonnull BodySource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<Modifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      @Nonnull Iterable<AnnotationUsage> annotations,
      @Nonnull Position position) {
    super(source, methodSignature, modifiers, thrownExceptions, annotations, position);
  }

  /** @return returns default value of annotation. May be null, if there is no default value */
  @Nullable
  public Object getDefaultValue() {
    return this.bodySource.resolveDefaultValue();
  }
}
