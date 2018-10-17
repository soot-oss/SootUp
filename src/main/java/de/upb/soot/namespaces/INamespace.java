package de.upb.soot.namespaces;

import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.namespaces.classprovider.IClassProvider;
import de.upb.soot.signatures.ClassSignature;

import java.util.Optional;

/**
 * Public interface to a namespace. Namespaces are sources for {@link SootClass}es, e.g. Java Classpath, Android APK, JAR
 * file, etc.
 * The strategy to traverse something.
 *
 * @author Manuel Benz created on 22.05.18
 * @author Ben Hermann
 * @author Linghui Luo
 *
 */
public interface INamespace {

  /**
   * Create or find a class source for a given signature.
   * @param signature The signature of the class to be found.
   * @return The source entry for that class.
   */
  Optional<AbstractClassSource> getClassSource(ClassSignature signature);

  /**
   * The class provider attached to this namespace.
   * @return An instance of {@link IClassProvider} to be used.
   */
  IClassProvider getClassProvider();
}
