package de.upb.swt.soot.core;

import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.views.View;
import java.util.*;
import java.util.function.Function;
import javax.annotation.Nonnull;

/** author: Markus Schmidt */
public class MutableProject<P extends Project, V extends View> extends Project {

  private final P project;
  private static final List<AnalysisInputLocation> inputLocation = new ArrayList<>();

  public MutableProject(P project) {
    // FIXME: we need to provide a reference to this class to forward getInputLocations to here..
    super(
        project.getLanguage(),
        Collections.emptyList(),
        project.getIdentifierFactory(),
        project.getSourceTypeSpecifier());
    this.project = project;

    /*
    TODO: We need to restructure Soots: Project/Subclasses -> returntype: generic <V extends View>
    TODO: Listener to have an MutableView, too (which wraps an instance of <V> )

    final Class<? extends View> c = View.class;
    Constructor<?> cons = null;
    try {
      cons = c.getConstructor(JavaView.class);
      View view = (View) cons.newInstance( project );

    } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
      e.printStackTrace();
    }
    */
  }

  void addClassSource(SootClassSource cs) {
    // addInputLocation(new EagerInputLocation(Collections.singletonMap(cs.getClassType(), cs)));
  }

  void addInputLocation(AnalysisInputLocation ana) {
    final boolean added = inputLocation.add(ana);
    if (added) {
      // TODO: fire events?
    }
  }

  void removeInputLocation(AnalysisInputLocation ana) {
    final boolean removed = inputLocation.remove(ana);
    if (removed) {
      // TODO: fire events?
    }
  }

  @Override
  @Nonnull
  public List<AnalysisInputLocation> getInputLocations() {
    return inputLocation;
  }

  @Override
  @Nonnull
  public View createFullView() {
    return new MutableView<>(project, project.createFullView());
  }

  @Override
  @Nonnull
  public View createOnDemandView() {
    return new MutableView<>(project, project.createOnDemandView());
  }

  @Override
  @Nonnull
  public View createOnDemandView(Function<AnalysisInputLocation, ClassLoadingOptions> function) {
    return new MutableView<>(project, project.createOnDemandView(function));
  }

  @Override
  @Nonnull
  public View createView(Scope scope) {
    return new MutableView<>(project, project.createView(scope));
  }
}
