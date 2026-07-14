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
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import sootup.core.cache.ClassCache;
import sootup.core.cache.FullCache;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
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
  @NonNull protected final JavaIdentifierFactory identifierFactory;

  @NonNull protected final List<AnalysisInputLocation> inputLocations;
  @NonNull protected final ClassCache cache;

  protected volatile boolean isFullyResolved = false;

  public JavaView(@NonNull AnalysisInputLocation inputLocation) {
    this(Collections.singletonList(inputLocation));
  }

  public JavaView(@NonNull List<AnalysisInputLocation> inputLocations) {
    this(inputLocations, new FullCacheProvider());
  }

  public JavaView(
      @NonNull List<AnalysisInputLocation> inputLocations,
      @NonNull ClassCacheProvider cacheProvider) {
    this(inputLocations, cacheProvider, JavaIdentifierFactory.getInstance());
  }

  protected JavaView(
      @NonNull List<AnalysisInputLocation> inputLocations,
      @NonNull ClassCacheProvider cacheProvider,
      @NonNull JavaIdentifierFactory idf) {
    this.inputLocations = inputLocations;
    this.cache = cacheProvider.createCache();
    this.identifierFactory = idf;
  }

  /** Resolves all classes that are part of the view and stores them in the cache. */
  @Override
  @NonNull
  public synchronized Stream<JavaSootClass> getClasses() {
    if (isFullyResolved && cache instanceof FullCache) {
      return cache.getClasses().stream().map(clazz -> (JavaSootClass) clazz);
    }

    Stream<JavaSootClass> resolvedClasses =
        inputLocations.stream()
            .flatMap(
                location -> {
                  // TODO: [ms] find a way to not stream().collect().stream()
                  return location.getClassSources(this).toList().stream();
                })
            .map(sootClassSource -> (JavaSootClassSource) sootClassSource)
            .map(this::buildClassFrom);
    isFullyResolved = true;
    return resolvedClasses;
  }

  /** Resolves the class matching the provided {@link ClassType ClassType}. */
  @Override
  @NonNull
  public synchronized Optional<JavaSootClass> getClass(@NonNull ClassType type) {
    JavaSootClass cachedClass = (JavaSootClass) cache.getClass(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }

    Optional<JavaSootClassSource> abstractClass = getClassSource(type);
    return abstractClass.map(this::buildClassFrom);
  }

  @Override
  @NonNull
  public Optional<JavaSootMethod> getMethod(@NonNull MethodSignature signature) {
    return getClass(signature.getDeclClassType())
        .flatMap(c -> c.getMethod(signature.getSubSignature()));
  }

  @Override
  @NonNull
  public Optional<JavaSootField> getField(@NonNull FieldSignature signature) {
    return getClass(signature.getDeclClassType())
        .flatMap(c -> c.getField(signature.getSubSignature()));
  }

  @NonNull
  @Override
  public JavaIdentifierFactory getIdentifierFactory() {
    return identifierFactory;
  }

  /** Returns the number of classes that are currently stored in the cache. */
  public int getCachedClassesCount() {
    return cache.size();
  }

  @NonNull
  protected Optional<JavaSootClassSource> getClassSource(@NonNull ClassType type) {
    // Process inputLocations in parallel but preserve the "first-in-list" semantics by
    // attaching indices and selecting the smallest index whose location produced a present
    // Optional. This keeps full parallelism while returning the earliest-match by input order.
    // Which actually matches the JVM behavior and is deterministic so nicer anyway.
    return IntStream.range(0, inputLocations.size())
        .parallel()
        .mapToObj(
            i -> new AbstractMap.SimpleEntry<>(i, inputLocations.get(i).getClassSource(type, this)))
        .filter(e -> e.getValue().isPresent())
        // pick the entry with the smallest original index
        .min(Comparator.comparingInt(e -> e.getKey()))
        .map(e -> (JavaSootClassSource) e.getValue().get());
  }

  @NonNull
  protected synchronized JavaSootClass buildClassFrom(JavaSootClassSource classSource) {

    ClassType classType = classSource.getClassType();
    JavaSootClass theClass;
    if (cache.hasClass(classType)) {
      theClass = (JavaSootClass) cache.getClass(classType);
    } else {
      theClass = classSource.buildClass(classSource.getAnalysisInputLocation().getSourceType());
      cache.putClass(classType, theClass);
    }
    return theClass;
  }
}
