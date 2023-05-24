package sootup.java.bytecode.interceptors.typeresolving.types;
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

import javax.annotation.Nonnull;
import sootup.core.jimple.visitor.TypeVisitor;
import sootup.core.types.Type;

/**
 * This type is imaginary type, and used for Type Inference
 *
 * @author Zun Wang
 */
public class BottomType extends Type {

  @Nonnull private static final BottomType INSTANCE = new BottomType();

  @Nonnull
  public static BottomType getInstance() {
    return INSTANCE;
  }

  private BottomType() {}

  @Override
  public void accept(@Nonnull TypeVisitor v) {
    // todo: add bottom type case
  }
}
