package sootup.interceptors.typeresolving.types;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2019-2022 Zun Wang
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
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.Type;

/**
 * This type is an imaginary/intermediate type which is used to calculate Type Inference
 *
 * @author Zun Wang
 */
public class BottomType extends Type {

  @NonNull private static final BottomType INSTANCE = new BottomType();

  @NonNull
  public static BottomType getInstance() {
    return INSTANCE;
  }

  private BottomType() {}

  @Override
  public <V extends TypeVisitor> V accept(@NonNull V v) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String toString() {
    return "BottomType";
  }
}
