package de.upb.soot.buildactor;

import static de.upb.soot.util.Utils.peek;

import de.upb.soot.Project;
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

  @Nonnull
  private JavaView buildJavaView() {
    return new JavaView(this.project);
  }

  @Nonnull
  public IView buildComplete() {
    return peek(this.buildJavaView(), JavaView::resolveAll);
  }

  @Nonnull
  public IView buildOnDemand() {
    return this.buildJavaView();
  }
}
