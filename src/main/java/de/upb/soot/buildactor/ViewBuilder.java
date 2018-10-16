package de.upb.soot.buildactor;

import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.util.NotYetImplementedException;
import de.upb.soot.views.IView;

/**
 * Bridges the process from bytecode representation to Soot IR (Jimple) representation
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Andreas Dann
 *
 */
public class ViewBuilder {
    private INamespace namespace;

    public ViewBuilder(INamespace namespace) {
        this.namespace = namespace;
    }

    public IView buildComplete() {
        IView result = null;
        // create a starting view
        // iterate over everything in the namespace
        // convert source representation to IR (Jimple) representation
        //      e.g. by calling the ClassBuilder
        // compose View

        /*
        for (AbstractClassSource cs : namespace.getAllSources()) {
            SootClass sc = cs.getContent().resolve(ResolvingLevel.BODIES, result);
            // Populate view
        }
        return result;
        */
        throw new NotYetImplementedException();
    }
}
