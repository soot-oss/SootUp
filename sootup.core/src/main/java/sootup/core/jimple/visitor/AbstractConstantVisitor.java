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

import javax.annotation.Nonnull;
import sootup.core.jimple.common.constant.*;

public class AbstractConstantVisitor<V> extends AbstractVisitor<V> implements ConstantVisitor {

  @Override
  public void caseBooleanConstant(@Nonnull BooleanConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseDoubleConstant(@Nonnull DoubleConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseFloatConstant(@Nonnull FloatConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseIntConstant(@Nonnull IntConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseLongConstant(@Nonnull LongConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseNullConstant(@Nonnull NullConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseStringConstant(@Nonnull StringConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseEnumConstant(@Nonnull EnumConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseClassConstant(@Nonnull ClassConstant constant) {
    defaultCaseConstant(constant);
  }

  @Override
  public void caseMethodHandle(@Nonnull MethodHandle handle) {
    defaultCaseConstant(handle);
  }

  @Override
  public void caseMethodType(@Nonnull MethodType methodType) {
    defaultCaseConstant(methodType);
  }

  @Override
  public void defaultCaseConstant(@Nonnull Constant constant) {}
}
