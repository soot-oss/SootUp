package sootup.core.jimple.visitor;

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

import org.jspecify.annotations.NonNull;
import sootup.core.types.*;

/**
 * @author Markus Schmidt
 */
public abstract class AbstractTypeVisitor implements TypeVisitor, Visitor {

  @Override
  public void caseBooleanType() {
    defaultCaseType();
  }

  @Override
  public void caseByteType() {
    defaultCaseType();
  }

  @Override
  public void caseCharType() {
    defaultCaseType();
  }

  @Override
  public void caseShortType() {
    defaultCaseType();
  }

  @Override
  public void caseIntType() {
    defaultCaseType();
  }

  @Override
  public void caseLongType() {
    defaultCaseType();
  }

  @Override
  public void caseDoubleType() {
    defaultCaseType();
  }

  @Override
  public void caseFloatType() {
    defaultCaseType();
  }

  @Override
  public void caseArrayType() {
    defaultCaseType();
  }

  @Override
  public void caseClassType(@NonNull ClassType classType) {
    defaultCaseType();
  }

  @Override
  public void caseNullType() {
    defaultCaseType();
  }

  @Override
  public void caseVoidType() {
    defaultCaseType();
  }

  @Override
  public void caseUnknownType() {
    defaultCaseType();
  }

  @Override
  public void defaultCaseType() {}
}
