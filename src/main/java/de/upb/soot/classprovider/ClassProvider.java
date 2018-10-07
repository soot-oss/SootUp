package de.upb.soot.classprovider;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;
import de.upb.soot.views.Scene;

import java.nio.file.Path;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public abstract class ClassProvider {

  // FIXME: that should be done by aka??
  /**
   * Resolve the given ClassSource to a SootClass.
   * 
   * @param srcNamespace
   *          to source's namespace
   * @param sourcePath
   *          the path to the source file
   * @param classSignature
   *          its signature
   * @return the resolved SootClass
   */
  // SootClass getSootClass(ClassSource classSource);

  public abstract ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  public abstract FileType getHandledFileType();

  public abstract SootClass reify(ClassSource classSource);

  public abstract de.upb.soot.core.SootClass resolve(de.upb.soot.core.SootClass sootClass);

  public abstract de.upb.soot.core.SootMethod resolveMethodBody(de.upb.soot.core.SootMethod sootMethod);

  public abstract Scene getScene();

  final public Iterable<de.upb.soot.core.SootClass> resolveSootClasses(de.upb.soot.signatures.ClassSignature[] sootClasses) {
    if (sootClasses == null) {
      return java.util.Collections.emptyList();
    }
    return java.util.Arrays.stream(sootClasses).map(p -> resolveSootClass(p)).collect(java.util.stream.Collectors.toList());
  }

  final public de.upb.soot.core.SootClass resolveSootClass(de.upb.soot.signatures.ClassSignature signature) {
    java.util.Optional<de.upb.soot.core.SootClass> moduleClass = getScene().getClass(signature);
    return moduleClass.get();
  }

  // currenty, hack for the ModuleFinder, must be ommited...
  final public java.util.Optional<de.upb.soot.core.SootClass>
      reifyScene(de.upb.soot.classprovider.ClassSource moduleInfoSource) {
    return getScene().reifyClass(moduleInfoSource);
  }
}
