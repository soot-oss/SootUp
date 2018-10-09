package de.upb.soot.namespaces;

import de.upb.soot.namespaces.classprovider.ClassSource;
import de.upb.soot.core.SootClass;
import de.upb.soot.signatures.ClassSignature;

import java.util.Optional;

/**
 * Public interface to a namespace. Namespaces are sources for {@link SootClass}es, e.g. Java Classpath, Android APK, JAR
 * file, etc.
 *
 * @author Manuel Benz created on 22.05.18
 */
public interface INamespace {
  /**
   * FIXME: a namespace should only return class sources Searches the namespace and sub-namespaces for all contained classes.
   * 
   * @return A collection of not-yet-resolved {@link SootClass}es
   */
  /*
   * Collection<SootClass> getClasses(SignatureFactory factory);
   * 
   *//**
      * Searches the namespace and all sub-namespaces for a {@link SootClass} matching the given {@link ClassSignature}.
      *
      * @param classSignature
      *          The {@link ClassSignature} denoting the searched {@link SootClass}
      * @return An optional containing the found class or empty if the class does not reside in this namespace
      *//*
         * Optional<SootClass> getClass(ClassSignature classSignature);
         */

  Optional<ClassSource> getClassSource(ClassSignature signature);
}
