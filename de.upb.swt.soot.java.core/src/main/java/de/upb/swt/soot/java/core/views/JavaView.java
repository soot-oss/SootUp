package de.upb.swt.soot.java.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Jan Martin Persch, Christian Brüggemann and others
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

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.AbstractClassSource;
import de.upb.swt.soot.core.frontend.ResolveException;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.inputlocation.ClassLoadingOptions;
import de.upb.swt.soot.core.transform.BodyInterceptor;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.views.AbstractView;
import de.upb.swt.soot.java.core.AnnotationUsage;
import de.upb.swt.soot.java.core.JavaAnnotationSootClass;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.types.AnnotationType;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * The Class JavaView manages the Java classes of the application being analyzed.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView<JavaSootClass> {

  @Nonnull private final Map<ClassType, JavaSootClass> cache = new HashMap<>();

  private volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation<JavaSootClass>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(@Nonnull Project<JavaView, JavaSootClass> project) {
    this(project, analysisInputLocation -> null);
  }

  /**
   * Creates a new instance of the {@link JavaView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JavaView(
      @Nonnull Project<JavaView, JavaSootClass> project,
      @Nonnull
          Function<AnalysisInputLocation<JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors(AnalysisInputLocation<JavaSootClass> clazz) {
    return this.classLoadingOptionsSpecifier.apply(clazz) != null
        ? this.classLoadingOptionsSpecifier.apply(clazz).getBodyInterceptors()
        : getBodyInterceptors();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors() {
    // TODO add default interceptors from
    // de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
    return Collections.emptyList();
  }

  @Override
  @Nonnull
  public synchronized Collection<JavaSootClass> getClasses() {
    resolveAll();
    return cache.values();
  }

  @Override
  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(@Nonnull ClassType type) {
    return getAbstractClass(type);
  }

  @Nonnull
  Optional<JavaSootClass> getAbstractClass(@Nonnull ClassType type) {
    JavaSootClass cachedClass = cache.get(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    final List<AbstractClassSource<JavaSootClass>> foundClassSources =
        getProject().getInputLocations().stream()
            .map(location -> location.getClassSource(type, this))
            .filter(Optional::isPresent)
            .limit(2)
            .map(Optional::get)
            .collect(Collectors.toList());

    if (foundClassSources.size() < 1) {
      return Optional.empty();
    } else if (foundClassSources.size() > 1) {
      throw new ResolveException(
          "Multiple class candidates for \""
              + type
              + "\" found in the given AnalysisInputLocations. Soot can't decide which AnalysisInputLocation it should refer to for this Type.\n"
              + "The candidates are "
              + foundClassSources.stream()
                  .map(cs -> cs.getSourcePath().toString())
                  .collect(Collectors.joining(",")),
          foundClassSources.get(0).getSourcePath());
    }
    return buildClassFrom(foundClassSources.get(0));
  }

  @Nonnull
  private synchronized Optional<JavaSootClass> buildClassFrom(
      AbstractClassSource<? extends JavaSootClass> classSource) {
    JavaSootClass theClass =
        cache.computeIfAbsent(
            classSource.getClassType(),
            type ->
                classSource.buildClass(getProject().getSourceTypeSpecifier().sourceTypeFor(type)));

    if (theClass.getType() instanceof AnnotationType) {
      JavaAnnotationSootClass jasc = (JavaAnnotationSootClass) theClass;
      jasc.getAnnotations(Optional.of(this)).forEach(AnnotationUsage::getValuesWithDefaults);
    }

    return Optional.of(theClass);
  }

  private synchronized void resolveAll() {
    if (isFullyResolved) {
      return;
    }

    getProject().getInputLocations().stream()
        .flatMap(location -> location.getClassSources(getIdentifierFactory(), this).stream())
        .forEach(this::buildClassFrom);
    isFullyResolved = true;
  }
}
