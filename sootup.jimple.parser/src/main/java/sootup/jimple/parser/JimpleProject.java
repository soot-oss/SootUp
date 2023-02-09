package sootup.jimple.parser;

import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import sootup.core.IdentifierFactory;
import sootup.core.Project;
import sootup.core.SourceTypeSpecifier;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.inputlocation.DefaultSourceTypeSpecifier;
import sootup.core.model.SootClass;

public class JimpleProject extends Project<SootClass<?>, JimpleView> {

  public JimpleProject(@Nonnull AnalysisInputLocation<? extends SootClass<?>> inputLocation) {
    super(JimpleLanguage.getInstance(), inputLocation, DefaultSourceTypeSpecifier.getInstance());
  }

  public JimpleProject(@Nonnull List<AnalysisInputLocation<? extends SootClass<?>>> inputLocation) {
    super(
        JimpleLanguage.getInstance(),
        inputLocation,
        JimpleLanguage.getInstance().getIdentifierFactory(),
        DefaultSourceTypeSpecifier.getInstance());
  }

  public JimpleProject(
      @Nonnull AnalysisInputLocation<? extends SootClass<?>> inputLocation,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(JimpleLanguage.getInstance(), inputLocation, sourceTypeSpecifier);
  }

  public JimpleProject(
      @Nonnull List<AnalysisInputLocation<? extends SootClass<?>>> inputLocations,
      @Nonnull IdentifierFactory identifierFactory,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(JimpleLanguage.getInstance(), inputLocations, identifierFactory, sourceTypeSpecifier);
  }

  @Nonnull
  @Override
  public JimpleView createFullView() {
    final JimpleView jimpleView = new JimpleView(this);
    jimpleView.getClasses();
    return jimpleView;
  }

  @Nonnull
  @Override
  public JimpleView createOnDemandView() {
    return new JimpleView(this);
  }

  @Nonnull
  @Override
  public JimpleView createOnDemandView(
      @Nonnull
          Function<AnalysisInputLocation<? extends SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    return new JimpleView(this, classLoadingOptionsSpecifier);
  }
}
