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

public class BodyInterceptorMetric {

  private long runtime;
  private long memoryUsage;

  public BodyInterceptorMetric(long runtime, long memoryUsage) {
    this.runtime = runtime;
    this.memoryUsage = memoryUsage;
  }

  public long getRuntime() {
    return runtime;
  }

  public void setRuntime(long runtime) {
    this.runtime = runtime;
  }

  public long getMemoryUsage() {
    return memoryUsage;
  }

  public void setMemoryUsage(long memoryUsage) {
    this.memoryUsage = memoryUsage;
  }
}
