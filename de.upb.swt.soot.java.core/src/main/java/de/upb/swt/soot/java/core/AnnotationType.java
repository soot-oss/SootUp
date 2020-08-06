package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.IdentifierFactory;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;

/**
 * This class represents Java Annotations (JSR-175).
 *
 * @author Markus Schmidt
 */

// TODO:[ms] move to a better place?
public class AnnotationType extends JavaClassType {

  /**
   * Internal: Constructs the fully-qualified ClassSignature. Instances should only be created by a
   * {@link IdentifierFactory}
   *
   * @param annotationName the simple name of the class, e.g., ClassA NOT my.package.ClassA
   * @param packageName the corresponding package
   */
  public AnnotationType(String annotationName, PackageName packageName) {
    super("@interface" + annotationName, packageName);
  }
}
