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
      @Nonnull List<AnalysisInputLocation<JavaSootClass>> inputLocations,
      @Nonnull List<ModuleInfoAnalysisInputLocation> moduleInputLocations,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(language, inputLocations, sourceTypeSpecifier);
    moduleInfoAnalysisInputLocation = moduleInputLocations;
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
          Function<AnalysisInputLocation<JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    return new JavaModuleView(this, classLoadingOptionsSpecifier);
  }
}
