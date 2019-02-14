package de.upb.soot.buildactor;

import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaOnDemandView;
import de.upb.soot.views.JavaView;

import javax.annotation.Nonnull;

/**
 * Bridges the process from bytecode representation to Soot IR (Jimple) representation
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Andreas Dann
 *
 */
public class ViewBuilder {
  private @Nonnull Project project;

  public ViewBuilder(@Nonnull Project project) {
    this.project = project;
  }

  public @Nonnull IView buildComplete() {
    IView result = new JavaView(this.project);
    
    // create a starting view
    // iterate over everything in the namespace
    // convert source representation to IR (Jimple) representation
    // e.g. by calling the ClassBuilder
    // compose View

    for (ClassSource cs : this.project.getNamespace().getClassSources(result.getSignatureFactory())) {
      try {
       AbstractClass abstactClass = cs.getContent().resolve(ResolvingLevel.BODIES, result);
      } catch (ResolveException e) {
        e.printStackTrace();
      }
      // Populate view
    }
    return result;
  }

  public @Nonnull IView buildOnDemand() {
    return new JavaOnDemandView(this.project);
  }
}
