package de.upb.swt.soot.callgraph.spark.pointsto;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.types.Type;
import java.util.Collections;
import java.util.Set;

public class EmptyPointsToSet implements PointsToSet {
  @Override
  public boolean isEmpty() {
    return true;
  }

  @Override
  public boolean hasNonEmptyIntersection(PointsToSet other) {
    return false;
  }

  @Override
  public Set<Type> possibleTypes() {
    return Collections.emptySet();
  }

  @Override
  public Set<String> possibleStringConstants() {
    return Collections.emptySet();
  }

  @Override
  public Set<ClassConstant> possibleClassConstants() {
    return Collections.emptySet();
  }
}
