package de.upb.swt.soot.java.core;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019 Linghui Luo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import de.upb.swt.soot.core.Scope;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
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

  public JavaView createOnDemandView(
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
