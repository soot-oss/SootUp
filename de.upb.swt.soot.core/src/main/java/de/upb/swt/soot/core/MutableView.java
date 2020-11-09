package de.upb.swt.soot.core;

import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.model.AbstractClass;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.AbstractView;
import de.upb.swt.soot.core.views.View;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;

/** author: Markus Schmidt */
public class MutableView<V extends View> extends AbstractView {

  private final V wrappedView;

  private final AtomicInteger edits = new AtomicInteger(0);
  private volatile int lastFullResolve = -1;
  private List<ViewChangeListener> changeListener = new LinkedList<>();

  public MutableView(@Nonnull Project project, @Nonnull V view) {
    super(project);
    this.wrappedView = view;
  }

  void fireChange(SootClass oldClass, SootClass newClass) {
    edits.incrementAndGet();
    if (newClass == null) {
      for (ViewChangeListener viewChangeListener : changeListener) {
        viewChangeListener.classRemoved(newClass);
      }
    } else {
      if (oldClass == null) {
        for (ViewChangeListener viewChangeListener : changeListener) {
          viewChangeListener.classAdded(newClass);
        }
      } else {
        for (ViewChangeListener viewChangeListener : changeListener) {
          viewChangeListener.classChanged(oldClass, newClass);
        }
      }
    }
  }

  void fireChange(SootMethod oldMethod, SootMethod newMethod) {
    edits.incrementAndGet();
    if (newMethod == null) {
      for (ViewChangeListener viewChangeListener : changeListener) {
        viewChangeListener.methodRemoved(newMethod);
      }
    } else {
      if (oldMethod == null) {
        for (ViewChangeListener viewChangeListener : changeListener) {
          viewChangeListener.methodAdded(newMethod);
        }
      } else {
        for (ViewChangeListener viewChangeListener : changeListener) {
          viewChangeListener.methodChanged(oldMethod, newMethod);
        }
      }
    }
  }

  void addChangeListener(ViewChangeListener scl) {
    changeListener.add(scl);
  }

  void removeChangeListener(ViewChangeListener scl) {
    changeListener.remove(scl);
  }

  @Nonnull
  @Override
  public Collection<? extends AbstractClass<? extends AbstractClassSource>> getClasses() {
    final Collection<? extends AbstractClass<? extends AbstractClassSource>> classes =
        wrappedView.getClasses();
    lastFullResolve = edits.get();
    return classes;
  }

  @Nonnull
  @Override
  public Optional<? extends AbstractClass<? extends AbstractClassSource>> getClass(
      @Nonnull ClassType classType) {
    return wrappedView.getClass(classType);
  }

  @Nonnull
  @Override
  public Optional<Scope> getScope() {
    return wrappedView.getScope();
  }

  @Nonnull
  @Override
  public IdentifierFactory getIdentifierFactory() {
    return wrappedView.getIdentifierFactory();
  }

  @Override
  public boolean doneResolving() {
    return edits.incrementAndGet() == lastFullResolve;
  }

  @Nonnull
  @Override
  public String quotedNameOf(@Nonnull String s) {
    return wrappedView.quotedNameOf(s);
  }
}
