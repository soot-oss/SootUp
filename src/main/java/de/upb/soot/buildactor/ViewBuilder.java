package de.upb.soot.buildactor;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.util.NotYetImplementedException;
import de.upb.soot.views.IView;

/**
 * Bridges the process from bytecode representation to Soot IR (Jimple) representation
 *
 * @author Linghui Luo
 * @author Ben Hermann
 */
public class ViewBuilder {
    private INamespace namespace;

    public ViewBuilder(INamespace namespace) {
        this.namespace = namespace;
    }

    public IView buildComplete() {

        throw new NotYetImplementedException();
    }
}
