package sootup.java.core.views;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import java.util.*;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.ViewChangeListener;
import sootup.core.cache.MutableClassCache;
import sootup.core.cache.provider.MutableFullCacheProvider;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.views.MutableView;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;

/**
 * This view, in contrast to other views, can be modified. Classes and methods can be added, removed
 * and replaced within the view.
 */
public class MutableJavaView extends JavaView implements MutableView {
  private final List<ViewChangeListener> changeListeners = new LinkedList<>();
  private static final @NonNull Logger logger = LoggerFactory.getLogger(MutableJavaView.class);

  public MutableJavaView(@NonNull AnalysisInputLocation inputLocation) {
    this(Collections.singletonList(inputLocation));
  }

  public MutableJavaView(@NonNull List<AnalysisInputLocation> inputLocations) {
    super(inputLocations, new MutableFullCacheProvider());
  }

  /**
   * Adds the provided class to the mutable view. If the provided class already exists in the view,
   * a warning will be logged and the provided class will not be added a second time.
   */
  public void addClass(JavaSootClass clazz) {
    ClassType classType = clazz.getClassSource().getClassType();
    if (this.cache.hasClass(classType)) {
      logger.warn("Class " + classType + " already exists in view.");
      return;
    }
    this.cache.putClass(classType, clazz);
    this.fireAddition(clazz);
  }

  /**
   * Removes the class that matches the provided {@link ClassType ClassType} from the mutable view.
   */
  public void removeClass(ClassType classType) {
    JavaSootClass removedClass =
        (JavaSootClass) ((MutableClassCache) this.cache).removeClass(classType);
    this.fireRemoval(removedClass);
  }

  /** Removes the provided class from the mutable view. */
  public void removeClass(JavaSootClass clazz) {
    ClassType classType = clazz.getClassSource().getClassType();
    this.removeClass(classType);
  }

  /** Removes the provided oldClass from the view and adds the provided newClass. */
  public void replaceClass(JavaSootClass oldClass, JavaSootClass newClass) {
    this.removeClass(oldClass);
    this.addClass(newClass);
  }

  /**
   * Removes the provided method from the respective class it belongs to that is within the view.
   */
  public void removeMethod(JavaSootMethod method) {
    ClassType classType = method.getDeclaringClassType();
    MethodSubSignature mss = method.getSignature().getSubSignature();

    JavaSootClass clazz = (JavaSootClass) this.cache.getClass(classType);
    if (clazz == null) {
      return;
    }

    Set<JavaSootMethod> methods = clazz.getMethods();
    Set<JavaSootMethod> filteredMethods =
        methods.stream()
            .filter(met -> !met.getSignature().getSubSignature().equals(mss))
            .collect(Collectors.toSet());
    JavaSootClass newClazz = clazz.withMethods(filteredMethods);

    this.replaceClass(clazz, newClazz);
    this.fireRemoval(method);
  }

  /** Adds the provided method to the respective class within the view. */
  public void addMethod(JavaSootMethod method) {
    ClassType classType = method.getDeclaringClassType();

    JavaSootClass clazz = (JavaSootClass) this.cache.getClass(classType);
    if (clazz == null) {
      return;
    }

    Set<JavaSootMethod> methods = clazz.getMethods();
    Set<JavaSootMethod> newMethods = new HashSet<>(methods);
    newMethods.add(method);
    JavaSootClass newClazz = clazz.withMethods(newMethods);

    this.replaceClass(clazz, newClazz);
    this.fireAddition(method);
  }

  /**
   * Removes the provided oldMethod from the respective class within the view and adds the provided
   * newMethod.
   */
  public void replaceMethod(JavaSootMethod oldMethod, JavaSootMethod newMethod) {
    this.removeMethod(oldMethod);
    this.addMethod(newMethod);
  }

  @Override
  public void addChangeListener(ViewChangeListener listener) {
    changeListeners.add(listener);
  }

  @Override
  public void removeChangeListener(ViewChangeListener listener) {
    changeListeners.remove(listener);
  }

  /**
   * Triggers an event in the {@link ViewChangeListener ViewChangeListener} class, when a class is
   * added to the view.
   */
  private void fireAddition(JavaSootClass clazz) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.classAdded(clazz);
    }
  }

  /**
   * Triggers an event in the {@link ViewChangeListener ViewChangeListener} class, when a method is
   * added to the view.
   */
  private void fireAddition(JavaSootMethod method) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.methodAdded(method);
    }
  }

  /**
   * Triggers an event in the {@link ViewChangeListener ViewChangeListener} class, when a class is
   * removed from the view.
   */
  private void fireRemoval(JavaSootClass clazz) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.classRemoved(clazz);
    }
  }

  /**
   * Triggers an event in the {@link ViewChangeListener ViewChangeListener} class, when a method is
   * removed from the view.
   */
  private void fireRemoval(JavaSootMethod method) {
    for (ViewChangeListener viewChangeListener : changeListeners) {
      viewChangeListener.methodRemoved(method);
    }
  }
}
