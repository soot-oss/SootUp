package de.upb.soot.views;

import de.upb.soot.Project;

/**
 * Abstract class for view.
 * 
 * @author Linghui Luo
 *
 */
public abstract class AbstractView implements IView {

  protected Project project;

  public AbstractView(Project project) {
    this.project = project;
  }
}
