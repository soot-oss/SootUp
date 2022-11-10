package de.upb.sse.sootup.java.core;

import de.upb.sse.sootup.core.frontend.BodySource;
import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.model.Position;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.types.ClassType;
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
