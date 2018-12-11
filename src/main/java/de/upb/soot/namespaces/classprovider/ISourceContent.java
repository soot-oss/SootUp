package de.upb.soot.namespaces.classprovider;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.views.IView;

/**
 * Converts a single source into Soot IR (Jimple)
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 */
public interface ISourceContent {
  public AbstractClass resolve(ResolvingLevel level, IView view);
}
