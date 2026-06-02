package sootup.core.signatures;

import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * A marker subclass for {@code MethodSignature}. Used to identify the fully qualified signature of
 * a polymorphic call site within the framework. Allows downstream analyses to check {@code (sig
 * instanceof PolymorphicMethodSignature)}.
 */
public class PolymorphicMethodSignature extends MethodSignature {

  /** Pass-through constructor to instantiate a polymorphic method signature. */
  public PolymorphicMethodSignature(
      @NonNull ClassType declaringClass,
      @NonNull String methodName,
      @NonNull Iterable<Type> parameters,
      @NonNull Type fqReturnType) {
    super(declaringClass, new PolymorphicMethodSubSignature(methodName, parameters, fqReturnType));
  }

  /**
   * Constructs a polymorphic method signature using an already instantiated polymorphic
   * sub-signature.
   */
  public PolymorphicMethodSignature(
      @NonNull ClassType declaringClass, @NonNull PolymorphicMethodSubSignature polySubSignature) {
    super(declaringClass, polySubSignature);
  }
}
