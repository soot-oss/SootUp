package de.upb.soot.namespaces.classprovider;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.FileType;

/**
 * Responsible for creating {@link ClassSource}es based on the handled file type (.class, .jimple, .java, .dex, etc).
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface IClassProvider {

  /**
   * Resolve the given ClassSource to a SootClass.
   * 
   * @param classSource
   *          to resource
   * @return the resolved SootClass
   */
  SootClass getSootClass(ClassSource classSource);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();
}
