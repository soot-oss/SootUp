package sootup.java.core.views;

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

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.Project;
import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.ClassLoadingOptions;
import sootup.core.inputlocation.EmptyClassLoadingOptions;
import sootup.core.transform.BodyInterceptor;
import sootup.core.types.ClassType;
import sootup.core.views.AbstractView;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaAnnotationSootClass;
import sootup.java.core.JavaSootClass;
import sootup.java.core.types.AnnotationType;

/**
 * The Class JavaView manages the Java classes of the application being analyzed. This view cannot
 * be altered after its creation.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView<JavaSootClass> {

  @Nonnull protected final ClassCache<JavaSootClass> cache;

  protected volatile boolean isFullyResolved = false;

  @Nonnull
  protected Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
      classLoadingOptionsSpecifier;

  public JavaView(@Nonnull Project<JavaSootClass, ? extends JavaView> project) {
    this(project, new FullCacheProvider<>());
  }

  /** Creates a new instance of the {@link JavaView} class. */
  public JavaView(
      @Nonnull Project<JavaSootClass, ? extends JavaView> project,
      @Nonnull ClassCacheProvider<JavaSootClass> cacheProvider) {
    this(project, cacheProvider, analysisInputLocation -> EmptyClassLoadingOptions.Default);
  }

  /**
   * Creates a new instance of the {@link JavaView} class.
   *
   * @param classLoadingOptionsSpecifier To use the default {@link ClassLoadingOptions} for an
   *     {@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   *     options.
   */
  public JavaView(
      @Nonnull Project<JavaSootClass, ? extends JavaView> project,
      @Nonnull
          Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    this(project, new FullCacheProvider<>(), classLoadingOptionsSpecifier);
  }

  public JavaView(
      @Nonnull Project<JavaSootClass, ? extends JavaView> project,
      @Nonnull ClassCacheProvider<JavaSootClass> cacheProvider,
      @Nonnull
          Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    super(project);
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
    this.cache = cacheProvider.createCache();
  }

  @Nonnull
  @Override
  public List<BodyInterceptor> getBodyInterceptors(AnalysisInputLocation clazz) {
    return this.classLoadingOptionsSpecifier.apply(clazz).getBodyInterceptors();
  }

  public void configBodyInterceptors(
      @Nonnull
          Function<AnalysisInputLocation<? extends JavaSootClass>, ClassLoadingOptions>
              classLoadingOptionsSpecifier) {
    this.classLoadingOptionsSpecifier = classLoadingOptionsSpecifier;
  }

  /** Resolves all classes that are part of the view and stores them in the cache. */
  @Override
  @Nonnull
  public synchronized Collection<JavaSootClass> getClasses() {
    return resolveAll();
  }

  /** Resolves the class matching the provided {@link ClassType ClassType}. */
  @Override
  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(@Nonnull ClassType type) {
    JavaSootClass cachedClass = cache.getClass(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    Optional<? extends AbstractClassSource<? extends JavaSootClass>> abstractClass =
        getAbstractClass(type);
    if (!abstractClass.isPresent()) {
      return Optional.empty();
    }

    return buildClassFrom(abstractClass.get());
  }

  /** Returns the amount of classes that are currently stored in the cache. */
  public int getAmountOfStoredClasses() {
    return cache.size();
  }

  @Nonnull
  protected Optional<? extends AbstractClassSource<? extends JavaSootClass>> getAbstractClass(
      @Nonnull ClassType type) {
    return getProject().getInputLocations().stream()
        .map(location -> location.getClassSource(type, this))
        .filter(Optional::isPresent)
        // like javas behaviour: if multiple matching Classes(ClassTypes) are found on the
        // classpath the first is returned (see splitpackage)
        .limit(1)
        .map(Optional::get)
        .findAny();
  }

  @Nonnull
  protected synchronized Optional<JavaSootClass> buildClassFrom(
      AbstractClassSource<? extends JavaSootClass> classSource) {

    ClassType classType = classSource.getClassType();
    JavaSootClass theClass;
    if (!cache.hasClass(classType)) {
      theClass =
          classSource.buildClass(getProject().getSourceTypeSpecifier().sourceTypeFor(classSource));
      cache.putClass(classType, theClass);
    } else {
      theClass = cache.getClass(classType);
    }

    if (theClass.getType() instanceof AnnotationType) {
      JavaAnnotationSootClass jasc = (JavaAnnotationSootClass) theClass;
      jasc.getAnnotations(Optional.of(this)).forEach(AnnotationUsage::getValuesWithDefaults);
    }

    return Optional.of(theClass);
  }

  @Nonnull
  protected synchronized Collection<JavaSootClass> resolveAll() {
    if (isFullyResolved && cache instanceof FullCache) {
      return cache.getClasses();
    }

    Collection<Optional<JavaSootClass>> resolvedClassesOpts =
        getProject().getInputLocations().stream()
            .flatMap(location -> location.getClassSources(this).stream())
            .map(this::buildClassFrom)
            .collect(Collectors.toList());

    Collection<JavaSootClass> resolvedClasses =
        resolvedClassesOpts.stream()
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    isFullyResolved = true;

    return resolvedClasses;
  }
}
