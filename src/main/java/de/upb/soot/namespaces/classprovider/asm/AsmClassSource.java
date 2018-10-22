package de.upb.soot.namespaces.classprovider.asm;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;

public class AsmClassSource extends AbstractClassSource {

  public AsmClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature) {
    super(srcNamespace, sourcePath, classSignature);
  }

}
