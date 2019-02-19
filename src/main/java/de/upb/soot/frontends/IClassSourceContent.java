package de.upb.soot.frontends;

import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.views.IView;

/**
 * Converts a single source into Soot IR (Jimple).
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Manuel Benz
 */
public interface IClassSourceContent {
  AbstractClass resolve(ResolvingLevel level, IView view) throws ResolveException;
}
