package de.upb.soot.namespaces.classprovider;

import de.upb.soot.namespaces.FileType;
import de.upb.soot.namespaces.INamespace;

import java.nio.file.Path;
import java.util.Optional;

/** Responsible for handling various types of class sources (.class, .jimple, .java, .dex, etc) */
public interface IClassProvider {

  // TODO does the class provider need the signature or does it generate one?

  /**
   *
   * @param ns
   * @param sourcePath
   *          Resolved path to the class that can be handled by this {@link IClassProvider}.
   * @return
   */
  Optional<ClassSource> getClass(INamespace ns, Path sourcePath);

  /**
   * Returns the file type that is handled by this provider, e.g. class, jimple, java
   * 
   * @return
   */
  FileType getHandledFileType();
}
