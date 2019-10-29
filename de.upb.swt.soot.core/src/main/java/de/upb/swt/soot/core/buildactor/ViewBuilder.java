package de.upb.swt.soot.core.buildactor;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.views.View;
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

  @Nonnull
  public View buildComplete() {
    // TODO [ms] commented out due to refactoring (language independence)
    /*
    JavaView<S> javaView = new JavaView<>(project);
    javaView.getClasses(); // Forces a full resolve
    return javaView;
     */
    return null;
  }

  @Nonnull
  public View buildOnDemand() {
    // TODO [ms] commented out due to refactoring (language independence)
    //    return new JavaView<S>(this.project);
    return null;
  }
}
