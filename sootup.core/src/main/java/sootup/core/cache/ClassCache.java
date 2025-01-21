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

import java.util.Collection;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import sootup.core.frontend.SootClassSource;
import sootup.core.model.SootClass;
import sootup.core.types.ClassType;

/** Interface for different caching strategies of resolved classes. */
public interface ClassCache<C extends SootClass> {

  /**
   * return the SootClass identified by classType
   * */
  C getClass(@Nonnull ClassType classType);

  /**
   * return all SootClasses stored in this Cache
   * */
  @Nonnull
  Collection<C> getClasses();

  /**
   * add the given SootClass into the Cache
   * */
  // TODO: simplify: classType can be retrieved from sootClass.getType()
  @Nullable
  C putClass(@Nonnull ClassType classType, @Nonnull C sootClass);

  /**
   * Check if the associated SootClass for ClassType exists in the Cache
   * */
  boolean hasClass(@Nonnull ClassType classType);

  int size();
}
