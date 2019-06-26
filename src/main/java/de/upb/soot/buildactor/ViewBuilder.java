package de.upb.soot.buildactor;

import static de.upb.soot.util.Utils.peek;

import de.upb.soot.Project;
import de.upb.soot.inputlocation.AnalysisInputLocation;
import de.upb.soot.views.JavaView;
import de.upb.soot.views.View;
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
  private JavaView<? extends AnalysisInputLocation> buildJavaView() {
    return new JavaView<>(this.project);
  }

  @Nonnull
  public View buildComplete() {
    return peek(this.buildJavaView(), JavaView::resolveAll);
  }

  @Nonnull
  public View buildOnDemand() {
    return this.buildJavaView();
  }
}
