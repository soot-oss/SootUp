package de.upb.swt.soot.callgraph.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Kadiray Karakaya and others
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

import de.upb.swt.soot.core.signatures.MethodSignature;
import javax.annotation.Nonnull;

public class CallGraphVertex {

  @Nonnull final MethodSignature methodSignature;

  public CallGraphVertex(@Nonnull MethodSignature methodSignature) {
    this.methodSignature = methodSignature;
  }

  @Nonnull
  public MethodSignature getMethodSignature() {
    return methodSignature;
  }

  @Override
  public String toString() {
    return "CallGraphVertex{" + "methodSignature=" + methodSignature + '}';
  }
}
