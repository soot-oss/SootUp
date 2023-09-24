package sootup.core.transform;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2020 Christian Brüggemann
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
import sootup.core.model.Body;
import sootup.core.views.View;

/** @see #interceptBody(Body.BodyBuilder, View) */
public interface BodyInterceptor {

  /**
   * Takes a BodyBuilder and may apply a transformation to it, for example removing unused local
   * variables.
   *
   * @param builder
   * @param view
   */
  void interceptBody(@Nonnull Body.BodyBuilder builder, @Nonnull View<?> view);
}
