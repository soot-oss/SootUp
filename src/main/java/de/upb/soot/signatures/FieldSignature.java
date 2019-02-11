package de.upb.soot.signatures;

/**
 * Represents the fully qualified signature of a field.
 * 
 * @author Linghui Luo
 */
public class FieldSignature extends AbstractClassMemberSignature {

  public FieldSignature(final String name, final JavaClassSignature declaringClass, final TypeSignature type) {
    super(name, declaringClass, type);
  }

  @Override
  public String getSubSignature() {
      return typeSignature.toString() +
              ' ' +
              name;
  }

}
