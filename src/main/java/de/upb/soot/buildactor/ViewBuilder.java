package de.upb.soot.buildactor;

import de.upb.soot.Project;
import de.upb.soot.core.AbstractClass;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.ResolveException;
import de.upb.soot.util.NotYetImplementedException;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import javax.annotation.Nonnull;

/**
 * Bridges the process from bytecode representation to Soot IR (Jimple) representation
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Andreas Dann
 */
public class ViewBuilder {
  private @Nonnull Project project;

  public ViewBuilder(@Nonnull Project project) {
    this.project = project;
  }

  public @Nonnull IView buildComplete() {
    throw new NotYetImplementedException();
  }

  public @Nonnull IView buildOnDemand() {
    IView result = new JavaView(this.project);

    // create a starting view
    // iterate over everything in the namespace
    // convert source representation to IR (Jimple) representation
    // e.g. by calling the ClassBuilder
    // compose View

    for (ClassSource cs :
        this.project.getNamespace().getClassSources(result.getSignatureFactory())) {
      try {
        AbstractClass abstractClass = cs.getContent().resolveClass(ResolvingLevel.HIERARCHY, result);
        
        result.addClass(abstractClass);
      } catch (ResolveException e) {
        e.printStackTrace();
      }
      // Populate view
    }
    return result;
  }
}
