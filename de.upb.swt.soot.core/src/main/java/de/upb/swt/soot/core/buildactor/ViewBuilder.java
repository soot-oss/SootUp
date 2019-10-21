package de.upb.swt.soot.core.buildactor;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.views.JavaView;
import de.upb.swt.soot.core.views.View;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
    return buildComplete(null);
  }

  @Nonnull
  public View buildComplete(@Nullable ClassLoadingOptions classLoadingOptions) {
    JavaView<S> javaView = new JavaView<>(project, classLoadingOptions);
    javaView.getClasses(); // Forces a full resolve
    return javaView;
  }

  @Nonnull
  public View buildOnDemand() {
    return buildOnDemand(null);
  }

  @Nonnull
  public View buildOnDemand(@Nullable ClassLoadingOptions classLoadingOptions) {
    return new JavaView<>(this.project, classLoadingOptions);
  }
}
