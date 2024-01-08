package sootup.java.core;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.frontend.BodySource;
import sootup.core.model.MethodModifier;
import sootup.core.model.Position;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;

public class JavaAnnotationSootMethod extends JavaSootMethod {

  public JavaAnnotationSootMethod(
      @Nonnull BodySource source,
      @Nonnull MethodSignature methodSignature,
      @Nonnull Iterable<MethodModifier> modifiers,
      @Nonnull Iterable<ClassType> thrownExceptions,
      @Nonnull Iterable<AnnotationUsage> annotations,
      @Nonnull Position position) {
    super(source, methodSignature, modifiers, thrownExceptions, annotations, position);
  }

  /** @return returns default value of annotation. May be null, if there is no default value */
  @Nullable
  public Object getDefaultValue() {
    return this.bodySource.resolveAnnotationsDefaultValue();
  }
}
