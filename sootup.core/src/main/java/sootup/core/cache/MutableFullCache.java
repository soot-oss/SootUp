package sootup.core.cache;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 1997 - 2024 Raja Vallée-Rai and others
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/**
 * Mutable version of the {@link FullCache} that additionally allows for a removal of cached
 * classes.
 */
public class MutableFullCache<C extends SootClass> extends FullCache<C>
    implements MutableClassCache<C> {

  /** set SootClass into Cache, if element exists it will be replaced. */
  @Override
  @Nullable
  public C putClass(@Nonnull C sootClass) {
    return cache.put(sootClass.getType(), sootClass);
  }

  /** removes the SootClass identified by classType from the Cache. */
  @Override
  public SootClass removeClass(@Nonnull ClassType classType) {
    if (this.hasClass(classType)) {
      return cache.remove(classType);
    }
    return null;
  }
}
