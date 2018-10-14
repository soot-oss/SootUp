package de.upb.soot.namespaces.classprovider.java;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;

/**
 * A class source for resolving from .java files using wala java source front-end.
 * 
 * @author Linghui Luo
 */
public class JavaClassSource extends AbstractClassSource {

  public JavaClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature) {
    super(srcNamespace, sourcePath, classSignature);
  }
}
