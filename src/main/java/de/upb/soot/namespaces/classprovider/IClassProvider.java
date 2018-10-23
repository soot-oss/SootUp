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


}
