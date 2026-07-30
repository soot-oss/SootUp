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
import sootup.core.frontend.SootClassSource;
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

  /**
   * Types that none of the {@link AnalysisInputLocation}s could provide a class source for. {@link
   * #cache} only memoizes successful resolutions, so without this every repeated lookup of an
   * absent type re-runs {@link #getClassSource(ClassType)}, which probes every input location -
   * including the JDK jimage filesystem - while holding this view's monitor. Call graph
   * construction against an incomplete classpath does exactly that: it repeats a handful of failing
   * lookups thousands of times.
   *
   * <p>Entries stay valid because the set of input locations is fixed for the lifetime of the view.
   * A subclass that makes a class available afterwards has to call {@link
   * #forgetAbsence(ClassType)} - see {@link MutableJavaView#addClass}.
   */
  @NonNull private final Set<ClassType> absentClasses = new HashSet<>();

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
      return cache.getClasses().map(clazz -> (JavaSootClass) clazz);
    }
    List<JavaSootClass> resolvedClasses =
        inputLocations.stream()
            .flatMap(
                location -> {
                  try (Stream<? extends SootClassSource> sources = location.getClassSources(this)) {
                    return sources.toList().stream();
                  }
                })
            .map(sootClassSource -> (JavaSootClassSource) sootClassSource)
            .map(this::buildClassFrom)
            .toList();

    isFullyResolved = true;
    return resolvedClasses.stream();
  }

  /** Resolves the class matching the provided {@link ClassType ClassType}. */
  @Override
  @NonNull
  public synchronized Optional<JavaSootClass> getClass(@NonNull ClassType type) {
    JavaSootClass cachedClass = (JavaSootClass) cache.getClass(type);
    if (cachedClass != null) {
      return Optional.of(cachedClass);
    }
    if (absentClasses.contains(type)) {
      return Optional.empty();
    }

    Optional<JavaSootClassSource> abstractClass = getClassSource(type);
    if (abstractClass.isEmpty()) {
      absentClasses.add(type);
      return Optional.empty();
    }
    return Optional.of(buildClassFrom(abstractClass.get()));
  }

  /**
   * Forgets that {@code type} was previously resolved as absent, so that the next {@link
   * #getClass(ClassType)} queries the input locations again. Subclasses that make a class available
   * after construction should call this.
   *
   * <p>It is not load-bearing for {@link MutableJavaView} as currently written: {@link
   * #getClass(ClassType)} consults {@link #cache} before {@link #absentClasses}, and {@code
   * addClass} populates that cache, so a stale absence record is shadowed anyway. It guards the
   * cases where that no longer holds - if the two checks are ever reordered, or if a mutating view
   * is given an evicting cache such as {@link sootup.core.cache.LRUCache}, where an added class can
   * disappear from the cache again and let the stale record surface.
   */
  protected synchronized void forgetAbsence(@NonNull ClassType type) {
    absentClasses.remove(type);
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
    JavaSootClass theClass = (JavaSootClass) cache.getClass(classType);
    if (theClass == null) {
      theClass = classSource.buildClass(classSource.getAnalysisInputLocation().getSourceType());
      cache.putClass(classType, theClass);
    }
    return theClass;
  }
}
