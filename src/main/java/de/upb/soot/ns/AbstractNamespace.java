package de.upb.soot.ns;

import de.upb.soot.IClassProvider;

/** @author Manuel Benz created on 22.05.18 */
public abstract class AbstractNamespace implements INamespace {
  protected final IClassProvider classProvider;

  public AbstractNamespace(IClassProvider classProvider) {
    this.classProvider = classProvider;
  }
}
