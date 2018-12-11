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
    StringBuilder sb = new StringBuilder();
    sb.append(typeSignature.toString());
    sb.append(' ');
    sb.append(name);
    return sb.toString();
  }

}
