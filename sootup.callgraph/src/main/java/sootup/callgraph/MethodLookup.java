package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.model.MemberResolution;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * JVM method lookup shared by all call graph / points-to algorithms, a cached front end of {@link
 * MemberResolution}:
 *
 * <ul>
 *   <li>{@link #find} resolves a method reference from its referenced class, see {@link
 *       MemberResolution#resolveMethod}. It may return an abstract method (e.g. a re-abstracted
 *       override); {@link #findConcrete} maps that to empty.
 *   <li>{@link #select} and {@link #dispatch} pick the method a receiver class runs for a call,
 *       which is not always the first one with the same sub-signature, see {@link
 *       MemberResolution#selectMethod}.
 * </ul>
 *
 * <p>Not pluggable on purpose: JVM semantics, one right answer. Pluggable target models
 * (reflection, invokedynamic) live elsewhere.
 *
 * <p>Cache: per (class, sub-signature) and per (receiver class, resolved method), misses included;
 * dropped when the type hierarchy modification count changes. One instance per analysis, holds the
 * view strongly.
 */
public final class MethodLookup {

  private static final Logger logger = LoggerFactory.getLogger(MethodLookup.class);

  @NonNull private final View view;
  @NonNull private final TypeHierarchy typeHierarchy;

  @NonNull
  private final Map<ClassType, Map<MethodSubSignature, Optional<SootMethod>>> cache =
      new ConcurrentHashMap<>();

  @NonNull
  private final Map<ClassType, Map<MethodSignature, Optional<SootMethod>>> selectionCache =
      new ConcurrentHashMap<>();

  private volatile long cacheModCount;

  public MethodLookup(@NonNull View view) {
    this.view = view;
    this.typeHierarchy = view.getTypeHierarchy();
    this.cacheModCount = typeHierarchy.getModificationCount();
  }

  @NonNull
  public View getView() {
    return view;
  }

  /** Lookup starting at the declaring class of {@code sig}. */
  @NonNull
  public Optional<SootMethod> find(@NonNull MethodSignature sig) {
    return find(sig.getDeclClassType(), sig.getSubSignature());
  }

  /** Lookup starting at {@code start}; may yield an abstract method. Cached. */
  @NonNull
  public Optional<SootMethod> find(@NonNull ClassType start, @NonNull MethodSubSignature subSig) {
    dropCachesIfHierarchyChanged();
    Map<MethodSubSignature, Optional<SootMethod>> perClass = perClass(cache, start);
    Optional<SootMethod> result = perClass.get(subSig);
    if (result == null) {
      // no computeIfAbsent: lookup reads other classes, keep map ops non-reentrant
      result = lookup(view, start, subSig);
      perClass.put(subSig, result);
    }
    return result;
  }

  /**
   * The method that a receiver of class {@code receiver} runs for a call to {@code resolved}, see
   * {@link MemberResolution#selectMethod}; empty if it would be abstract. Cached.
   */
  @NonNull
  public Optional<SootMethod> select(@NonNull ClassType receiver, @NonNull SootMethod resolved) {
    dropCachesIfHierarchyChanged();
    Map<MethodSignature, Optional<SootMethod>> perClass = perClass(selectionCache, receiver);
    Optional<SootMethod> result = perClass.get(resolved.getSignature());
    if (result == null) {
      result = MemberResolution.selectMethod(view, receiver, resolved).map(SootMethod.class::cast);
      perClass.put(resolved.getSignature(), result);
    }
    return result;
  }

  /**
   * Virtual dispatch of the call {@code sig} on a receiver of class {@code receiver}: resolves
   * {@code sig}, then {@link #select selects}. If {@code sig} does not resolve, e.g. because its
   * class is not in the view, falls back to {@link #findConcrete(ClassType, MethodSubSignature)}.
   */
  @NonNull
  public Optional<SootMethod> dispatch(@NonNull ClassType receiver, @NonNull MethodSignature sig) {
    Optional<SootMethod> resolved = find(sig);
    if (resolved.isEmpty()) {
      return findConcrete(receiver, sig.getSubSignature());
    }
    return select(receiver, resolved.get());
  }

  private void dropCachesIfHierarchyChanged() {
    long modCount = typeHierarchy.getModificationCount();
    if (modCount != cacheModCount) {
      cache.clear();
      selectionCache.clear();
      cacheModCount = modCount;
    }
  }

  @NonNull
  private static <K> Map<K, Optional<SootMethod>> perClass(
      Map<ClassType, Map<K, Optional<SootMethod>>> cache, ClassType type) {
    Map<K, Optional<SootMethod>> perClass = cache.get(type);
    if (perClass == null) {
      perClass = new ConcurrentHashMap<>(4);
      Map<K, Optional<SootMethod>> prev = cache.putIfAbsent(type, perClass);
      if (prev != null) {
        perClass = prev;
      }
    }
    return perClass;
  }

  /** Like {@link #find(ClassType, MethodSubSignature)}, abstract result mapped to empty. */
  @NonNull
  public Optional<SootMethod> findConcrete(
      @NonNull ClassType start, @NonNull MethodSubSignature subSig) {
    return find(start, subSig).filter(m -> !m.isAbstract());
  }

  /** Like {@link #find(MethodSignature)}, abstract result mapped to empty. */
  @NonNull
  public Optional<SootMethod> findConcrete(@NonNull MethodSignature sig) {
    return find(sig).filter(m -> !m.isAbstract());
  }

  // ---- uncached algorithm ----

  /** Uncached lookup, see class doc. Logs a warning on miss. */
  @NonNull
  static Optional<SootMethod> lookup(
      @NonNull View view, @NonNull ClassType start, @NonNull MethodSubSignature subSig) {
    if (!view.getClass(start).isPresent()) {
      logger.warn("Could not find \"{}\" to resolve \"{}\"", start, subSig);
      return Optional.empty();
    }
    Optional<SootMethod> m =
        MemberResolution.resolveMethod(
                view, view.getIdentifierFactory().getMethodSignature(start, subSig))
            .map(SootMethod.class::cast);
    if (m.isEmpty()) {
      logger.warn(
          "Could not find \"{}\" in {} and in its superclasses and interfaces",
          subSig,
          start.getClassName());
    }
    return m;
  }

  /** Start class and its superclass chain; stops at first class not in the view. */
  @NonNull
  static Optional<SootMethod> findInSuperclasses(
      @NonNull View view, @NonNull SootClass start, @NonNull MethodSubSignature subSig) {
    SootClass c = start;
    while (c != null) {
      Optional<? extends SootMethod> m = c.getMethod(subSig);
      if (m.isPresent()) {
        return Optional.of(m.get());
      }
      c = c.getSuperclass().flatMap(view::getClass).orElse(null);
    }
    return Optional.empty();
  }

  /** Maximally specific method among the interfaces implemented by {@code start}. */
  @NonNull
  static Optional<SootMethod> findDefault(
      @NonNull View view, @NonNull SootClass start, @NonNull MethodSubSignature subSig) {
    return MemberResolution.resolveInSuperinterfaces(view, start.getType(), subSig)
        .map(SootMethod.class::cast);
  }
}
