package de.upb.swt.soot.core.types;

/**
 * Represents the signature of a Class
 *
 * @author Markus Schmidt
 */
public abstract class ClassType extends ReferenceType {
  public abstract boolean isBuiltInClass();

  public abstract String getFullyQualifiedName();

  public abstract String getClassName();
}
