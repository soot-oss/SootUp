package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.List;
import javax.annotation.Nonnull;

/** Represents the fully qualified signature of a method. */
public class MethodSignature extends AbstractClassMemberSignature {

  public MethodSignature(
      JavaClassType declaringClassSignature,
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
      final @Nonnull JavaClassType declaringClass, final @Nonnull MethodSubSignature subSignature) {
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
  public List<Type> getParameterSignatures() {
    return this.getSubSignature().getParameterSignatures();
  }

  // FIXME: [JMP] Implement quotation
  @Nonnull
  public String toQuotedString() {
    return this.toString();
  }
}
