package de.upb.soot.namespaces.classprovider;

import de.upb.soot.views.IView;

/**
 * Converts a single source into Soot IR (Jimple)
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface ISourceContent {
  public de.upb.soot.core.SootClass resolve(de.upb.soot.core.ResolvingLevel level, IView view);
}
