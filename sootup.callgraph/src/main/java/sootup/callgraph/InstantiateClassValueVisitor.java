package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2023 Jonas Klauke
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
import sootup.core.jimple.common.expr.JNewArrayExpr;
import sootup.core.jimple.common.expr.JNewExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.visitor.AbstractValueVisitor;
import sootup.core.types.ArrayType;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

public class InstantiateClassValueVisitor extends AbstractValueVisitor<ClassType> {

  public void init() {
    setResult(null);
  }

  @Override
  public void caseNewExpr(@Nonnull JNewExpr expr) {
    setResult(expr.getType());
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
    setResult(findClassTypeInType(expr.getBaseType()));
  }

  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {
    setResult(findClassTypeInType(expr.getBaseType()));
  }

  private ClassType findClassTypeInType(Type type) {
    if (type instanceof ArrayType) {
      return findClassTypeInType(((ArrayType) type).getBaseType());
    }
    if (type instanceof ClassType) {
      return (ClassType) type;
    }
    return null;
  }
}
