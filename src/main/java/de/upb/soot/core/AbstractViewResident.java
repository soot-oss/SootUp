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

  @Override
  public IView getView() {
    return view;
  }

  @Override
  public void setView(IView view) {
    this.view = view;
  }

}
