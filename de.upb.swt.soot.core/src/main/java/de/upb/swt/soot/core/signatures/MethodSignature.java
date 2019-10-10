package de.upb.swt.soot.core.signatures;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.types.JavaClassType;
import de.upb.swt.soot.core.types.Type;
import java.util.EnumSet;
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
    this(declaringClass, subSignature, EnumSet.noneOf(Modifier.class));
  }

  public MethodSignature(
      final @Nonnull JavaClassType declaringClass,
      final @Nonnull MethodSubSignature subSignature,
      EnumSet<Modifier> modifiers) {
    super(declaringClass, subSignature, modifiers);
  }

  @Override
  @Nonnull
  public MethodSubSignature getSubSignature() {
    return (MethodSubSignature) super.getSubSignature();
  }

  /** The method's parameters' signatures. */
  @Nonnull
  public List<Type> getParameterSignatures() {
    return getSubSignature().getParameterSignatures();
  }

  // FIXME: [JMP] Implement quotation
  @Nonnull
  public String toQuotedString() {
    return this.toString();
  }
}
