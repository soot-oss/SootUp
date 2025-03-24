package sootup.interceptors;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallée-Rai, Christian Brüggemann
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
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;
import sootup.interceptors.typeresolving.TypeResolver;
import sootup.java.core.views.JavaView;

/**
 * This transformer assigns types to local variables.
 *
 * @author Zun Wang
 */
public class TypeAssigner implements BodyInterceptor {

  public TypeAssigner() {}

  @Override
  public void interceptBody(Body.@NonNull BodyBuilder builder, @NonNull View view) {
    new TypeResolver((JavaView) view).resolve(builder);
  }
}
