package de.upb.soot.buildactor;

import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.namespaces.INamespace;
import de.upb.soot.namespaces.classprovider.AbstractClassSource;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaOnDemandView;

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
    // e.g. by calling the ClassBuilder
    // compose View

    for (AbstractClassSource cs : namespace.getClassSources(result.getSignatureFactory())) {
      cs.getContent().resolve(ResolvingLevel.BODIES, result);
      // Populate view
    }
    return result;
  }

  public IView buildOnDemand() {
    // FIXME: why do we need a project for a view?
    return new JavaOnDemandView(null, namespace);
  }
}
