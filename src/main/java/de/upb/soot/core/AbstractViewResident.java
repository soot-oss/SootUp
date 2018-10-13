package de.upb.soot.core;

import de.upb.soot.views.IView;

/**
 * Abstract class for anything lives in a View
 * 
 * @author Linghui Luo
 *
 */
public class AbstractViewResident implements IViewResident {

  private IView view;

  public AbstractViewResident(IView view) {
    this.view = view;
  }

  @Override
  public IView getView() {
    return view;
  }
}
