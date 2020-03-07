package de.upb.swt.soot.core.types;

import de.upb.swt.soot.core.signatures.PackageName;

/**
 * Represents the signature of a Class
 *
 * @author Markus Schmidt
 */
public abstract class ClassType extends ReferenceType {
  public abstract boolean isBuiltInClass();

  public abstract String getFullyQualifiedName();

  public abstract String getClassName();

  public abstract PackageName getPackageName();
}
