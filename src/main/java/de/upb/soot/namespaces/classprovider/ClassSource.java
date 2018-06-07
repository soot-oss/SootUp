package de.upb.soot.namespaces.classprovider;

import static com.google.common.base.Preconditions.checkNotNull;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

/**
 * Basic class for storing information that is needed to resolve a {@link de.upb.soot.core.SootClass}.
 *
 * @author Manuel Benz created on 22.05.18
 **/
public abstract class ClassSource {
  private final INamespace srcNamespace;
  protected ClassSignature classSignature;

  /**
   * Creates a {@link ClassSource} which resides in the given {@link INamespace}.
   * 
   * @param srcNamespace
   *          The {@link INamespace} that handles this source
   * @param classSignature
   *          The {@link ClassSignature} of the to-be-resolved {@link de.upb.soot.core.SootClass}
   */
  public ClassSource(INamespace srcNamespace, ClassSignature classSignature) {
    checkNotNull(srcNamespace);

    this.srcNamespace = srcNamespace;
    this.classSignature = classSignature;
  }

  public ClassSignature getClassSignature() {
    return classSignature;
  }
}
