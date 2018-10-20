package de.upb.soot.namespaces.classprovider;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.JavaClassSignature;

import java.nio.file.Path;

/**
 * Responsible for creating {@link AbstractClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface IClassProvider {

  // TODO: needed to create references to other classes ... or init the resolving process...
  de.upb.soot.views.IView view = null;

  AbstractClassSource createClassSource(INamespace srcNamespace, Path sourcePath, JavaClassSignature classSignature);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();

  /**
   * Create or provide a representation of the actual manifestation of the class.
   * 
   * @return
   */
  de.upb.soot.namespaces.classprovider.ISourceContent getContent(AbstractClassSource classSource);

  /**
   * TODO AD: Methods the ASM classProvider may implement to resolve a class
   *
   */

  public abstract AbstractClass reify(AbstractClassSource classSource);

  public abstract AbstractClass resolve(AbstractClass sootClass);

  public abstract de.upb.soot.core.SootMethod resolveMethodBody(de.upb.soot.core.SootMethod sootMethod);

  /**
   * Resolves an array of classes.
   *
   * @param sootClasses
   *          signatures of the classes to resolve
   * @return the resolved classses
   */
  default public Iterable<AbstractClass>
      resolveSootClasses(de.upb.soot.signatures.JavaClassSignature[] sootClasses) {
    if (sootClasses == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(sootClasses).map(p -> resolveSootClass(p)).collect(java.util.stream.Collectors.toList());
  }

  /**
   * Resolve a SootClass.
   *
   * @param signature
   *          the signature of the class to resolve
   * @return the resolved class
   */
  default public AbstractClass resolveSootClass(de.upb.soot.signatures.JavaClassSignature signature) {
    java.util.Optional<AbstractClass> moduleClass = view.getClass(signature);
    return moduleClass.get();
  }

}
