package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.jimple.common.constant.*;

public abstract class AbstractConstantVisitor<T> implements ConstantVisitor {

  T result;

  @Override
  public void caseBooleanConstant(BooleanConstant v) { defaultCase(v); }

  @Override
  public void caseDoubleConstant(DoubleConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseFloatConstant(FloatConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseIntConstant(IntConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseLongConstant(LongConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseNullConstant(NullConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseStringConstant(StringConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseClassConstant(ClassConstant v) {
    defaultCase(v);
  }

  @Override
  public void caseMethodHandle(MethodHandle v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Object v) {
  }

  public T getResult() {
    return result;
  }

  public void setResult(T result) {
    this.result = result;
  }
}
