package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.SourceTypeSpecifier;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;

public class JavaModuleProject extends JavaProject {

  @Nonnull private final List<ModuleInfoAnalysisInputLocation> moduleInfoAnalysisInputLocation;

  public JavaModuleProject(
      JavaLanguage language,
      @Nonnull List<AnalysisInputLocation<? extends JavaSootClass>> inputLocations,
      @Nonnull List<ModuleInfoAnalysisInputLocation> moduleInputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(language, inputLocations, sourceTypeSpecifier);
    moduleInfoAnalysisInputLocation = moduleInputLocations;
  }

  @Override
  public void validate() {
    if (getInputLocations().isEmpty() && getModuleInfoAnalysisInputLocation().isEmpty()) {
      throw new IllegalArgumentException(
          "The inputLocations collection for classPath and modulePath must not be empty.");
    }
  }

  @Nonnull
  public List<ModuleInfoAnalysisInputLocation> getModuleInfoAnalysisInputLocation() {
    return moduleInfoAnalysisInputLocation;
  }

  @Nonnull
  @Override
  public JavaModuleView createOnDemandView() {
    return new JavaModuleView(this);
  }

  @Nonnull
  @Override
  public JavaModuleView createOnDemandView(
      @Nonnull
          Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    return new JavaModuleView(this, classLoadingOptionsSpecifier);
  }
}
