package de.upb.swt.soot.java.core.views;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * Java view since Java 9.
 *
 * @author Linghui Luo
 */
public class JavaModuleView extends JavaView {

  public JavaModuleView(Project project) {
    super(project);
  }

  public JavaModuleView(
      @Nonnull Project project,
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    super(project, classLoadingOptionsSpecifier);
  }
}
