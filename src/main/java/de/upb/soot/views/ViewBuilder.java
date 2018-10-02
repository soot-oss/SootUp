package de.upb.soot.views;

import de.upb.soot.namespaces.INamespace;
import de.upb.soot.util.NotYetImplementedException;

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
