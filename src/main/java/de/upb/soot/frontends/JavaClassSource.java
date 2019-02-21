package de.upb.soot.frontends;

import java.nio.file.Path;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.JavaClassSignature;

/**
 * A class source for resolving from .java files using wala java source front-end.
 * 
 * @author Linghui Luo
 */
public class JavaClassSource extends ClassSource {

  public JavaClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature) {
    super(srcNamespace, sourcePath, classSignature);
  }
}