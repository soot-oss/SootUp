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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.annotation.Nonnull;
import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.frontend.AbstractClassSource;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.views.AbstractView;
import sootup.java.core.*;

/**
 * The Class JavaView manages the Java classes of the application being analyzed. This view cannot
 * be altered after its creation.
 *
 * @author Linghui Luo created on 31.07.2018
 * @author Jan Martin Persch
 */
public class JavaView extends AbstractView {
  @Nonnull protected final JavaIdentifierFactory identifierFactory;

  @Nonnull protected final List<AnalysisInputLocation> inputLocations;
  @Nonnull protected final ClassCache cache;

  protected volatile boolean isFullyResolved = false;

  public JavaView(@Nonnull AnalysisInputLocation inputLocation) {
    this(Collections.singletonList(inputLocation));
  }

  public JavaView(@Nonnull List<AnalysisInputLocation> inputLocations) {
    this(inputLocations, new FullCacheProvider());
  }

  public JavaView(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull ClassCacheProvider cacheProvider) {
    this(inputLocations, cacheProvider, JavaIdentifierFactory.getInstance());
  }

  protected JavaView(
      @Nonnull List<AnalysisInputLocation> inputLocations,
      @Nonnull ClassCacheProvider cacheProvider,
      @Nonnull JavaIdentifierFactory idf) {
    this.inputLocations = inputLocations;
    this.cache = cacheProvider.createCache();
    this.identifierFactory = idf;
  }

  /** Resolves all classes that are part of the view and stores them in the cache. */
  @Override
  @Nonnull
  public synchronized Stream<JavaSootClass> getClasses() {
    if (isFullyResolved && cache instanceof FullCache) {
      return cache.getClasses().stream().map(clazz -> (JavaSootClass) clazz);
    }

    Stream<JavaSootClass> resolvedClasses =
        inputLocations.stream()
            .flatMap(location -> location.getClassSources(this).stream())
            .map(this::buildClassFrom)
            .filter(Optional::isPresent)
            .map(Optional::get);

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

    Optional<JavaSootClassSource> abstractClass = getClassSource(type);
    return abstractClass.flatMap(this::buildClassFrom);
  }

  @Nonnull
  public Optional<JavaAnnotationSootClass> getAnnotationClass(@Nonnull ClassType type) {
    return getClass(type).filter(sc -> sc.isAnnotation()).map(sc -> (JavaAnnotationSootClass) sc);
  }

  @Override
  @Nonnull
  public Optional<JavaSootMethod> getMethod(@Nonnull MethodSignature signature) {
    return getClass(signature.getDeclClassType())
        .flatMap(c -> c.getMethod(signature.getSubSignature()));
  }

  @Override
  @Nonnull
  public Optional<JavaSootField> getField(@Nonnull FieldSignature signature) {
    return getClass(signature.getDeclClassType())
        .flatMap(c -> c.getField(signature.getSubSignature()));
  }

  @Nonnull
  @Override
  public JavaIdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  /** Returns the number of classes that are currently stored in the cache. */
  public int getCachedClassesCount() {
    return cache.size();
  }

  @Nonnull
  protected Optional<JavaSootClassSource> getClassSource(@Nonnull ClassType type) {
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
          (JavaSootClass)
              classSource.buildClass(classSource.getAnalysisInputLocation().getSourceType());
      cache.putClass(classType, theClass);
    }
    return Optional.of(theClass);
  }
}
