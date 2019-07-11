package de.upb.soot.typehierarchy;

import de.upb.soot.types.JavaClassType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Represents a type hierarchy
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface TypeHierarchy {

  /**
   * Returns all classes that implement the specified interface. This is transitive: If class <code>
   * A extends B</code> and <code>B implements interface</code>, then this method will return both A
   * and B as implementers of <code>interfaceType</code>.
   */
  Set<JavaClassType> implementersOf(@Nonnull JavaClassType interfaceType);

  /**
   * Returns all classes that extend the specified class. This is transitive: If <code>A extends B
   * </code> and <code>B extends classType</code>, then this method will return both A and B as
   * extenders of <code>classType</code>.
   */
  Set<JavaClassType> extendersOf(@Nonnull JavaClassType classType);

  /**
   * Returns the interfaces implemented by <code>classType</code>. This includes interfaces
   * implemented by superclasses.
   */
  Set<JavaClassType> implementedInterfacesOf(@Nonnull JavaClassType classType);

  /**
   * Returns the direct superclass of <code>classType</code>. If <code>classType == java.lang.Object
   * </code>, this method returns null.
   */
  @Nullable
  JavaClassType superClassOf(@Nonnull JavaClassType classType);

  /**
   * Returns all superclasses of <code>classType</code> up to <code>java.lang.Object</code>, which
   * will be the last entry in the list.
   */
  default List<JavaClassType> superClassesOf(@Nonnull JavaClassType classType) {
    List<JavaClassType> superClasses = new ArrayList<>();
    JavaClassType currentSuperClass = superClassOf(classType);
    while (currentSuperClass != null) {
      superClasses.add(currentSuperClass);
      currentSuperClass = superClassOf(currentSuperClass);
    }
    return superClasses;
  }
}
