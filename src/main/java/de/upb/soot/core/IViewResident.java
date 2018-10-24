package de.upb.soot.core;

import de.upb.soot.views.IView;

/**
 * Interface for anything that lives in a view such as classes, fields, methods, types, expressions etc.
 * 
 * @author Linghui Luo
 *
 */
public interface IViewResident {

  IView getView();
}
