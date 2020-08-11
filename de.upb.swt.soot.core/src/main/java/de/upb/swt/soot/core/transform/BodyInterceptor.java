package de.upb.swt.soot.core.transform;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019 Christian Brüggemann
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

import de.upb.swt.soot.core.model.Body;
import javax.annotation.Nonnull;

/**
 * @author
 * @see #interceptBody(Body)
 */
public interface BodyInterceptor {

  /**
   * Takes a body and may apply a transformation to it, for example removing unused local variables.
   * Since {@link Body} is immutable, this needs to create a new instance.
   *
   * <p>In case no transformation is applied, this method may return the original body it received
   * as its parameter.
   *
   * <p><b>Warning:</b> Implementations of this method must not modify the original body or any of
   * its contents.
   */
  @Nonnull
  Body interceptBody(@Nonnull Body originalBody);
}
