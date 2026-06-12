package sootup.java.core.views;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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
import org.jspecify.annotations.NonNull;
import sootup.core.cache.provider.ClassCacheProvider;
import sootup.core.cache.provider.FullCacheProvider;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;

public class JavaEagerView extends JavaView {

  public JavaEagerView(@NonNull AnalysisInputLocation inputLocation) {
    this(Collections.singletonList(inputLocation));
  }

  public JavaEagerView(@NonNull List<AnalysisInputLocation> inputLocations) {
    this(inputLocations, new FullCacheProvider());
  }

  public JavaEagerView(
      @NonNull List<AnalysisInputLocation> inputLocations,
      @NonNull ClassCacheProvider cacheProvider) {
    super(inputLocations, cacheProvider, JavaIdentifierFactory.getInstance());
    eagerLoadClasses();
  }

  protected void eagerLoadClasses() {
      getClasses()
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

    // All class data is now in the FullCache — release file-system resources.
    for (AnalysisInputLocation loc : inputLocations) {
      try {
        loc.close();
      } catch (Exception e) {
        throw new RuntimeException("Failed to close input location after eager load", e);
      }
    }
  }
}
