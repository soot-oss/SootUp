package sootup.java.core.views;

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.Project;
import sootup.core.ViewChangeListener;
import sootup.core.cache.MutableClassCache;
import sootup.core.cache.provider.MutableFullCacheProvider;
import sootup.core.model.SootMethod;
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
  private static final @Nonnull Logger logger = LoggerFactory.getLogger(MutableJavaView.class);

  public MutableJavaView(@Nonnull Project<JavaSootClass, ? extends JavaView> project) {
    super(project, new MutableFullCacheProvider<>());
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
        ((MutableClassCache<JavaSootClass>) this.cache).removeClass(classType);
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

    JavaSootClass clazz = this.cache.getClass(classType);
    if (clazz == null) return;

    Set<? extends JavaSootMethod> methods = clazz.getMethods();
    Set<SootMethod> filteredMethods =
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

    JavaSootClass clazz = this.cache.getClass(classType);
    if (clazz == null) return;

    Set<? extends JavaSootMethod> methods = clazz.getMethods();
    Set<SootMethod> newMethods = new HashSet<>(methods);
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
