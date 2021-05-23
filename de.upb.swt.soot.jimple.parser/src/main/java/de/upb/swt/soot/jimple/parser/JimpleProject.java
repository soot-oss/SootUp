package de.upb.swt.soot.jimple.parser;

import de.upb.swt.soot.core.*;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.inputlocation.DefaultSourceTypeSpecifier;
import de.upb.swt.soot.core.model.SootClass;
import java.util.List;
import java.util.function.Function;
import javax.annotation.Nonnull;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class JimpleProject extends Project<JimpleView, SootClass<?>> {

  public JimpleProject(@Nonnull AnalysisInputLocation<SootClass<?>> inputLocation) {
    super(JimpleLanguage.getInstance(), inputLocation, DefaultSourceTypeSpecifier.getInstance());
  }

  public JimpleProject(@Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocation) {
    super(
        JimpleLanguage.getInstance(),
        inputLocation,
        JimpleLanguage.getInstance().getIdentifierFactory(),
        DefaultSourceTypeSpecifier.getInstance());
  }

  public JimpleProject(
      @Nonnull AnalysisInputLocation<SootClass<?>> inputLocation,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    super(JimpleLanguage.getInstance(), inputLocation, sourceTypeSpecifier);
  }

  public JimpleProject(
      @Nonnull List<AnalysisInputLocation<SootClass<?>>> inputLocations,
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
          Function<AnalysisInputLocation<SootClass<?>>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    return new JimpleView(this, classLoadingOptionsSpecifier);
  }

  @Nonnull
  @Override
  public JimpleView createView(Scope s) {
    throw new NotImplementedException();
  }
}
