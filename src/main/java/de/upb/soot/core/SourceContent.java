package de.upb.soot.core;

import de.upb.soot.views.IView;

/**
 * Converts a single source into Soot IR (Jimple)
 *
 * @author Andreas Dann
 * @author Linghui Luo
 * @author Ben Hermann
 */
public abstract class SourceContent {
    public abstract SootClass resolve(ResolvingLevel level, IView view);
}
