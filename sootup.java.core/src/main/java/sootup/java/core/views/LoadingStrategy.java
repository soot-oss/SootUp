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

import java.util.Optional;
import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;

/**
 * Determines how a {@link JavaView} resolves its classes: on demand (lazily, on first lookup) or
 * eagerly (all at once, at construction time). Injected into {@link JavaView}'s constructor,
 * mirroring how a {@link sootup.core.cache.provider.ClassCacheProvider} supplies the view's {@link
 * sootup.core.cache.ClassCache}.
 *
 * <p>Implementations live in {@code sootup.java.core.views}, the same package as {@link JavaView},
 * because {@link #resolveOnCacheMiss} and the default {@link #initialize} need to call {@link
 * JavaView}'s {@code protected} {@code getClassSource}/{@code buildClassFrom}/{@code
 * inputLocations} through a {@code JavaView}-typed parameter - which only compiles for same-package
 * callers, not arbitrary subclasses (JLS 6.6.2).
 */
public interface LoadingStrategy {

  @NonNull
  static LoadingStrategy onDemand() {
    return new OnDemandLoadingStrategy();
  }

  @NonNull
  static LoadingStrategy eager() {
    return new EagerLoadingStrategy();
  }

  /**
   * Called exactly once, as the last statement of {@link JavaView}'s constructor, after all of
   * {@code JavaView}'s own fields ({@code inputLocations}, {@code cache}, {@code loadingStrategy},
   * {@code identifierFactory}) have been assigned. The default does nothing; only the eager
   * strategy overrides this to resolve every class up front.
   *
   * <p>Because this runs before any subclass's own constructor body, any virtual call this makes on
   * {@code view} must not depend on subclass fields that a subclass constructor would otherwise
   * have initialized first. Do not wire the eager strategy into a {@link JavaView} subclass that
   * overrides {@code getClasses()} or {@code buildClassFrom(...)} without re-checking this.
   */
  default void initialize(@NonNull JavaView view) {}

  /**
   * Called from {@link JavaView#getClass(ClassType)} after a cache miss, while holding the owning
   * view's monitor. Returns the resolved class, or {@link Optional#empty()} if {@code type} cannot
   * be resolved.
   */
  @NonNull Optional<JavaSootClass> resolveOnCacheMiss(
      @NonNull JavaView view, @NonNull ClassType type);

  /**
   * Called from {@link JavaView#forgetAbsence(ClassType)}, itself called e.g. when {@link
   * MutableJavaView#addClass} makes a previously-absent type available. The default does nothing,
   * which is correct for the eager strategy: it tracks no absence state to forget, since a class
   * absent after an eager load can never become present.
   */
  default void makeAvailable(@NonNull ClassType type) {}
}
