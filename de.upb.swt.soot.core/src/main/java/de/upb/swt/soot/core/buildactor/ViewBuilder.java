package de.upb.swt.soot.core.buildactor;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.views.JavaView;
import de.upb.swt.soot.core.views.View;
import java.util.function.Function;
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
  public View buildComplete(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    JavaView<S> javaView = new JavaView<>(project, classLoadingOptionsSpecifier);
    javaView.getClasses(); // Forces a full resolve
    return javaView;
  }

  @Nonnull
  public View buildOnDemand() {
    return new JavaView<>(this.project);
  }

  @Nonnull
  public View buildOnDemand(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    return new JavaView<>(this.project, classLoadingOptionsSpecifier);
  }
}
