package de.upb.soot.core.buildactor;

import de.upb.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.soot.core.Project;
import de.upb.soot.core.views.JavaView;
import de.upb.soot.core.views.View;

import javax.annotation.Nonnull;

/**
 * Bridges the process from bytecode representation to Soot IR (Jimple) representation
 *
 * @author Linghui Luo
 * @author Ben Hermann
 * @author Andreas Dann
 */
public class ViewBuilder<S extends AnalysisInputLocation> {
  private @Nonnull Project<S> project;

  public ViewBuilder(@Nonnull Project<S> project) {
    this.project = project;
  }

  @Nonnull
  public View buildComplete() {
    JavaView<S> javaView = new JavaView<>(project);
    javaView.getClasses(); // Forces a full resolve
    return javaView;
  }

  @Nonnull
  public View buildOnDemand() {
    return new JavaView<>(this.project);
  }
}
