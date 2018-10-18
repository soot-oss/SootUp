package de.upb.soot.core;

import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.views.IView;

/**
 * Abstract class represents a class/module lives in {@link IView}.
 * 
 * @author Linghui Luo
 *
 */
public abstract class AbstractClass extends AbstractViewResident {

  protected final AbstractClassSource classSource;

  public AbstractClass(IView view, AbstractClassSource cs) {
    super(view);
    this.classSource = cs;
  }

  public AbstractClassSource getClassSource() {
    return classSource;
  }

  public abstract String getName();

}
