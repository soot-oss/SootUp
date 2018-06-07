package de.upb.soot.namespaces.classprovider;

import static com.google.common.base.Preconditions.checkNotNull;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

public abstract class ClassSource {
  private final INamespace srcNamespace;
  protected ClassSignature classSignature;

  public ClassSource(INamespace srcNamespace, ClassSignature classSignature) {
    checkNotNull(srcNamespace);

    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
  }

  public ClassSignature getClassSignature() {
    return classSignature;
  }
}
