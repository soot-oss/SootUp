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
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.typehierarchy.TypeHierarchy;
import sootup.core.types.ClassType;
import sootup.core.views.View;

/**
 * JVM method lookup shared by all call graph / points-to algorithms: from a start class (declared
 * owner for resolution, receiver type for dispatch) find the method a sub-signature binds to.
 *
 * <p>Order: start class and superclasses, then maximally specific interface method (default
 * methods), then signature-polymorphic methods ({@link SignaturePolymorphicMethods}). May return an
 * abstract method (e.g. re-abstracted override); use {@link #findConcrete} for dispatch.
 *
 * <p>Not pluggable on purpose: JVM semantics, one right answer. Pluggable target models
 * (reflection, invokedynamic) live elsewhere.
 *
 * <p>Cache: per (class, sub-signature), misses included; dropped when the type hierarchy
 * modification count changes. One instance per analysis, holds the view strongly.
 */
public final class MethodLookup {

  private static final Logger logger = LoggerFactory.getLogger(MethodLookup.class);

  @NonNull private final View view;
  @NonNull private final TypeHierarchy typeHierarchy;

  @NonNull
  private final Map<ClassType, Map<MethodSubSignature, Optional<SootMethod>>> cache =
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
    long modCount = typeHierarchy.getModificationCount();
    if (modCount != cacheModCount) {
      cache.clear();
      cacheModCount = modCount;
    }
    Map<MethodSubSignature, Optional<SootMethod>> perClass = cache.get(start);
    if (perClass == null) {
      perClass = new ConcurrentHashMap<>(4);
      Map<MethodSubSignature, Optional<SootMethod>> prev = cache.putIfAbsent(start, perClass);
      if (prev != null) {
        perClass = prev;
      }
    }
    Optional<SootMethod> result = perClass.get(subSig);
    if (result == null) {
      // no computeIfAbsent: lookup reads other classes, keep map ops non-reentrant
      result = lookup(view, start, subSig);
      perClass.put(subSig, result);
    }
    return result;
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
    SootClass startClass = view.getClass(start).orElse(null);
    if (startClass == null) {
      logger.warn("Could not find \"{}\" to resolve \"{}\"", start, subSig);
      return Optional.empty();
    }
    Optional<SootMethod> m = findInSuperclasses(view, startClass, subSig);
    if (m.isPresent()) {
      return m;
    }
    m = findDefault(view, startClass, subSig);
    if (m.isPresent()) {
      return m;
    }
    // call-site descriptor of MethodHandle/VarHandle polymorphic call never matches declaration
    m =
        SignaturePolymorphicMethods.resolveInHierarchy(view, startClass, subSig.getName())
            .map(SootMethod.class::cast);
    if (m.isPresent()) {
      return m;
    }
    logger.warn(
        "Could not find \"{}\" in {} and in its superclasses and interfaces",
        subSig,
        start.getClassName());
    return Optional.empty();
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

  /**
   * Maximally specific method among all interfaces implemented by {@code start}: the one whose
   * declaring interface is a subtype of all other candidates' interfaces.
   */
  @NonNull
  static Optional<SootMethod> findDefault(
      @NonNull View view, @NonNull SootClass start, @NonNull MethodSubSignature subSig) {
    TypeHierarchy typeHierarchy = view.getTypeHierarchy();
    return typeHierarchy
        .implementedInterfacesOf(start.getType())
        .flatMap(
            itf ->
                view.getClass(itf).flatMap(c -> c.getMethod(subSig)).stream()
                    .map(SootMethod.class::cast))
        .reduce(
            (currentLeastSubMethod, m) ->
                typeHierarchy.isSubtype(
                        currentLeastSubMethod.getDeclaringClassType(), m.getDeclaringClassType())
                    ? m
                    : currentLeastSubMethod);
  }
}
