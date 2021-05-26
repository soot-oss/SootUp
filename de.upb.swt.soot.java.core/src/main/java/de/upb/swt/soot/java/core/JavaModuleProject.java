package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.List;
import javax.annotation.Nonnull;

public class JavaModuleProject extends JavaView {
  @Nonnull private final List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocation;

  public JavaModuleProject(@Nonnull Project<? extends JavaView, JavaSootClass> project) {
    super(project);
    moduleInfoAnalysisInputLocation = null;
  }
}
