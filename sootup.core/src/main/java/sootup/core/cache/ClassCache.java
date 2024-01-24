package sootup.core.cache;

import java.util.Collection;
import javax.annotation.Nonnull;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Interface for different caching strategies of resolved classes. */
public interface ClassCache {

  SootClass getClass(ClassType classType);

  @Nonnull
  Collection<SootClass> getClasses();

  void putClass(ClassType classType, SootClass sootClass);

  boolean hasClass(ClassType classType);

  int size();
}
