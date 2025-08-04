package sootup.core.transform;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2024 Sahil Agichani
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

import org.jspecify.annotations.NonNull;
import sootup.core.model.Body;
import sootup.core.views.View;

public class RunTimeBodyInterceptor implements BodyInterceptor {

  private BodyInterceptorMetric biMetric = new BodyInterceptorMetric(0L, 0L);

  private final BodyInterceptor bodyInterceptor;

  public RunTimeBodyInterceptor(BodyInterceptor bodyInterceptor) {
    this.bodyInterceptor = bodyInterceptor;
  }

  public BodyInterceptorMetric getBiMetric() {
    return biMetric;
  }

  public BodyInterceptor getBodyInterceptor() {
    return bodyInterceptor;
  }

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    long startTime = System.currentTimeMillis(); // Start time
    final int MB = 1024 * 1024;
    Runtime runtime = Runtime.getRuntime();
    long usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();

    bodyInterceptor.interceptBody(builder, view);

    long endTime = System.currentTimeMillis(); // End time
    long duration = endTime - startTime;
    long usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
    long memoryUsed = (usedMemoryAfter - usedMemoryBefore) / MB;

    biMetric.setRuntime(biMetric.getRuntime() + duration);
    biMetric.setMemoryUsage(biMetric.getMemoryUsage() + memoryUsed);
  }
}
