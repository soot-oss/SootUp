package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo and others
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
import sootup.core.jimple.common.constant.*;

public class AbstractConstantVisitor implements ConstantVisitor, Visitor {

  @Override
  public void caseBooleanConstant(@NonNull BooleanConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseDoubleConstant(@NonNull DoubleConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseFloatConstant(@NonNull FloatConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseIntConstant(@NonNull IntConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseLongConstant(@NonNull LongConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseNullConstant(@NonNull NullConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseStringConstant(@NonNull StringConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseEnumConstant(@NonNull EnumConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseClassConstant(@NonNull ClassConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseMethodHandle(@NonNull MethodHandle handle) {
    defaultCaseConstant(handle);
  }

  @Override
  public void caseMethodType(@NonNull MethodType methodType) {
    defaultCaseConstant(methodType);
  }

  @Override
  public void defaultCaseConstant(@NonNull Constant constant) {}
}
