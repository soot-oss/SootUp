package sootup.java.core.views;

/*-
 * #%L
 * SootUp
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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootClassSource;

/**
 * Lazily resolves classes on first lookup, the default {@link LoadingStrategy} for {@link
 * JavaView}.
 *
 * <p>Memoizes types that no {@link sootup.core.inputlocation.AnalysisInputLocation} could provide a
 * class source for. The owning view's positive {@code cache} only memoizes successful resolutions,
 * so without this every repeated lookup of an absent type would re-run {@code getClassSource},
 * which probes every input location - including the JDK jimage filesystem - while holding the
 * view's monitor. Call graph construction against an incomplete classpath does exactly that: it
 * repeats a handful of failing lookups thousands of times.
 *
 * <p>Not thread-safe on its own; callers only touch {@link #absentClasses} while holding the owning
 * {@link JavaView}'s monitor (see {@link JavaView#getClass(ClassType)} / {@link
 * JavaView#forgetAbsence(ClassType)}, both {@code synchronized}). Do not share one instance of this
 * class across multiple {@link JavaView}s - each view's absence record is instance state, not a
 * shared cache.
 */
public class OnDemandLoadingStrategy implements LoadingStrategy {

  // TODO: [ms] if all classtypes are generated via IdentifierFactory we can use IdentityHashSet
  @NonNull protected final Set<ClassType> absentClasses = new HashSet<>();

  @Override
  @NonNull
  public Optional<JavaSootClass> resolveOnCacheMiss(
      @NonNull JavaView view, @NonNull ClassType type) {
    if (absentClasses.contains(type)) {
      return Optional.empty();
    }

    Optional<JavaSootClassSource> classSourceOpt = view.getClassSource(type);
    if (classSourceOpt.isEmpty()) {
      absentClasses.add(type);
      return Optional.empty();
    }
    return Optional.of(view.buildClassFrom(classSourceOpt.get()));
  }

  @Override
  public void makeAvailable(@NonNull ClassType type) {
    absentClasses.remove(type);
  }
}
