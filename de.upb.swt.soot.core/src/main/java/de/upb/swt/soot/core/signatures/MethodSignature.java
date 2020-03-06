package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents the fully qualified signature of a method. */
public class MethodSignature extends AbstractClassMemberSignature {

  public MethodSignature(
      ClassType declaringClassSignature,
      String methodName,
      Iterable<Type> parameters,
      Type fqReturnType) {
    this(declaringClassSignature, new MethodSubSignature(methodName, parameters, fqReturnType));
  }

  /**
   * Internal: Constructs a MethodSignature. Instances should only be created by a {@link
   * IdentifierFactory}
   *
   * @param declaringClass the declaring class signature
   * @param subSignature the sub-signature
   */
  public MethodSignature(
      final @Nonnull ClassType declaringClass, final @Nonnull MethodSubSignature subSignature) {
    super(declaringClass, subSignature);

    this._subSignature = subSignature;
  }

  private final @Nonnull MethodSubSignature _subSignature;

  @Override
  @Nonnull
  public MethodSubSignature getSubSignature() {
    return _subSignature;
  }

  /** The method's parameters' signatures. */
  @Nonnull
  public List<Type> getParameterTypes() {
    return this.getSubSignature().getParameterTypes();
  }
}
