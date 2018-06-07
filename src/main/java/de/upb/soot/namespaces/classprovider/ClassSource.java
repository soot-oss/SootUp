package de.upb.soot.namespaces.classprovider;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

import com.google.common.base.Preconditions;

public abstract class ClassSource {
  private final INamespace srcNamespace;
  protected ClassSignature classSignature;

  public ClassSource(INamespace srcNamespace, ClassSignature classSignature) {
    Preconditions.checkNotNull(srcNamespace);

    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
  }

  // public abstract Dependencies resolve(SootClass sc);

  public void close() {
  }

  public ClassSignature getClassSignature() {
    return classSignature;
  }
}
