/*
 * @author Linghui Luo
 */
package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaModuleView;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.function.Function;
import javax.annotation.Nonnull;

/**
 * The Class JavaViewBuilder builds views for given project.
 *
 * @author Linghui Luo
 */
public class JavaViewBuilder {

  /** The use java modules. */
  private boolean useJavaModules;

  /** The project. */
  private JavaProject project;

  /**
   * Instantiates a new java view builder.
   *
   * @param p the project
   */
  public JavaViewBuilder(JavaProject p) {
    this.project = p;
    JavaLanguage language = (JavaLanguage) p.getLanguage();
    this.useJavaModules = language.useJavaModules();
  }

  /**
   * Creates the on demand view.
   *
   * @return the java view
   */
  @Nonnull
  public JavaView createOnDemandView() {
    return chooseView();
  }

  public View createOnDemandView(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    return chooseView(classLoadingOptionsSpecifier);
  }

  /**
   * Creates the full view.
   *
   * @return the java view
   */
  @Nonnull
  public JavaView createFullView() {
    JavaView view = chooseView();
    view.getClasses();
    return view;
  }

  /**
   * Creates the view.
   *
   * @param s the s
   * @return the java view
   */
  @Nonnull
  public JavaView createView(Scope s) {
    return null;
  }

  /**
   * Choose view.
   *
   * @return the java view
   */
  @Nonnull
  private JavaView chooseView() {
    if (useJavaModules) {
      return new JavaModuleView(project);
    } else {
      return new JavaView(project);
    }
  }

  /**
   * Choose view.
   *
   * @return the java view
   */
  @Nonnull
  private JavaView chooseView(
      @Nonnull Function<AnalysisInputLocation, ClassLoadingOptions> classLoadingOptionsSpecifier) {
    if (useJavaModules) {
      return new JavaModuleView(project, classLoadingOptionsSpecifier);
    } else {
      return new JavaView(project, classLoadingOptionsSpecifier);
    }
  }
}
