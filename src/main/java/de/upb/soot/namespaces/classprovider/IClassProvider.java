package de.upb.soot.namespaces.classprovider;

import de.upb.soot.Options;
import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.signatures.ClassSignature;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface IClassProvider {

  // FIXME: that should be done by aka??
  /**
   * Resolve the given ClassSource to a SootClass.
   * 
   * @param classSource
   *          to resource
   * @return the resolved SootClass
   */
  // SootClass getSootClass(ClassSource classSource);

  ClassSource createClassSource(INamespace srcNamespace, Path sourcePath, ClassSignature classSignature);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();

  Optional<SootClass> resolve(ClassSource classSource);

}
