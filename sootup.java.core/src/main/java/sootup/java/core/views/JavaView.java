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

import java.util.*;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import sootup.core.SourceTypeSpecifier;
import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.DefaultSourceTypeSpecifier;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.AbstractView;
import sootup.java.core.*;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.types.AnnotationType;

/**
 * The Class JavaView manages the Java classes of the application being analyzed. This view cannot
 * be altered after its creation.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView {

  @Nonnull protected final List<AnalysisInputLocation> inputLocations;
  @Nonnull protected final ClassCache cache;
  @Nonnull protected final SourceTypeSpecifier sourceTypeSpecifier;

  protected volatile boolean isFullyResolved = false;

  public JavaView(@Nonnull AnalysisInputLocation inputLocation) {
    this(Collections.singletonList(inputLocation));
  }

  public JavaView(@Nonnull List<AnalysisInputLocation> inputLocations) {
    this(inputLocations, new FullCacheProvider());
  }

  /**
   * Creates a new instance of the {@link JavaView} class.
   *
   * <p>{@link AnalysisInputLocation}, simply return <code>null</code>, otherwise the desired
   * options.
   */
  public JavaView(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull ClassCacheProvider cacheProvider) {
    this(inputLocations, cacheProvider, DefaultSourceTypeSpecifier.getInstance());
  }

  public JavaView(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull ClassCacheProvider cacheProvider,
      @Nonnull SourceTypeSpecifier sourceTypeSpecifier) {
    this.inputLocations = inputLocations;
    this.cache = cacheProvider.createCache();
    this.sourceTypeSpecifier = sourceTypeSpecifier;
  }

  /** Resolves all classes that are part of the view and stores them in the cache. */
  @Override
  @Nonnull
  public synchronized Collection<JavaSootClass> getClasses() {
    if (isFullyResolved && cache instanceof FullCache) {
      return cache
          .getClasses()
          .stream()
          .map(clazz -> (JavaSootClass) clazz)
          .collect(Collectors.toList());
    }

    Collection<JavaSootClass> resolvedClasses =
        inputLocations
            .stream()
            .flatMap(location -> location.getClassSources(this).stream())
            .map(this::buildClassFrom)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .collect(Collectors.toList());

    isFullyResolved = true;

    return resolvedClasses;
  }

  /** Resolves the class matching the provided {@link ClassType ClassType}. */
  @Override
  @Nonnull
  public synchronized Optional<JavaSootClass> getClass(@Nonnull ClassType type) {
    JavaSootClass cachedClass = (JavaSootClass) cache.getClass(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    Optional<JavaSootClassSource> abstractClass = getAbstractClass(type);
    return abstractClass.flatMap(this::buildClassFrom);
  }

  @Override
  @Nonnull
  public Optional<JavaSootMethod> getMethod(@Nonnull MethodSignature signature) {
    final Optional<JavaSootClass> aClass = getClass(signature.getDeclClassType());
      if (aClass.isPresent()) {
          return aClass.get().getMethod(signature.getSubSignature());
      }
      return Optional.empty();
  }

  @Override
  @Nonnull
  public Optional<JavaSootField> getField(@Nonnull FieldSignature signature) {
    final Optional<JavaSootClass> aClass = getClass(signature.getDeclClassType());
      if (aClass.isPresent()) {
          return aClass.get().getField(signature.getSubSignature());
      }
      return Optional.empty();
  }

  @Nonnull
  @Override
  public JavaIdentifierFactory getIdentifierFactory() {
    return (JavaIdentifierFactory) new JavaLanguage(8).getIdentifierFactory();
  }

  /** Returns the number of classes that are currently stored in the cache. */
  public int getNumberOfStoredClasses() {
    return cache.size();
  }

  @Nonnull
  protected Optional<JavaSootClassSource> getAbstractClass(@Nonnull ClassType type) {
    return inputLocations
        .parallelStream()
        .map(location -> location.getClassSource(type, this))
        .filter(Optional::isPresent)
        // like javas behaviour: if multiple matching Classes(ClassTypes) are found on the
        // classpath the first is returned (see splitpackage)
        .limit(1)
        .map(Optional::get)
        .map(classSource -> (JavaSootClassSource) classSource)
        .findAny();
  }

  @Nonnull
  protected synchronized Optional<JavaSootClass> buildClassFrom(AbstractClassSource classSource) {

    ClassType classType = classSource.getClassType();
    JavaSootClass theClass;
    if (cache.hasClass(classType)) {
      theClass = (JavaSootClass) cache.getClass(classType);
    } else {
      theClass =
          (JavaSootClass) classSource.buildClass(sourceTypeSpecifier.sourceTypeFor(classSource));
      cache.putClass(classType, theClass);
    }

    if (theClass.getType() instanceof AnnotationType) {
      JavaAnnotationSootClass jasc = (JavaAnnotationSootClass) theClass;
      jasc.getAnnotations(Optional.of(this)).forEach(AnnotationUsage::getValuesWithDefaults);
    }

    return Optional.of(theClass);
  }

}
