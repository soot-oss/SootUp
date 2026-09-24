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
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.types.ClassType;
import sootup.java.core.JavaSootClass;

/**
 * Resolves every class of a {@link JavaView} up front, at construction time, then releases the
 * view's input locations. Used by {@link JavaEagerView}.
 *
 * <p>Since {@link #initialize} has already resolved (and cached) everything reachable, a class not
 * found in the cache by the time {@link #resolveOnCacheMiss} runs will never be found - there is
 * nothing left to probe, and the input locations are closed besides. This carries no absence-set
 * bookkeeping, unlike {@link OnDemandLoadingStrategy}, because none is needed.
 */
public class EagerLoadingStrategy implements LoadingStrategy {

  @Override
  public void initialize(@NonNull JavaView view) {
    view.getClasses()
        .forEach(
            c -> {
              c.getModifiers();
              c.getFields();
              c.getInterfaces();
              c.getAnnotations();
              c.getSuperclass();
              c.getOuterClass();
              c.getPosition();
              c.getMethods()
                  .forEach(
                      m -> {
                        if (m.hasBody()) {
                          m.getBody();
                        }
                      });
            }); // forces loading

    // All class data is now in the cache - release file-system resources.
    for (AnalysisInputLocation loc : view.inputLocations) {
      try {
        loc.close();
      } catch (Exception e) {
        throw new RuntimeException("Failed to close input location after eager load", e);
      }
    }
  }

  @Override
  @NonNull
  public Optional<JavaSootClass> resolveOnCacheMiss(
      @NonNull JavaView view, @NonNull ClassType type) {
    return Optional.empty();
  }
}
