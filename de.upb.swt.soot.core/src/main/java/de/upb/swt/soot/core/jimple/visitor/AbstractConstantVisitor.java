/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Etienne Gagnon
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.MethodHandle;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;

public abstract class AbstractConstantVisitor implements ConstantVisitor {
  Object result; // FIXME: Why is this an Object? What is this for?

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
  public void defaultCase(Object v) {}

  public Object getResult() {
    return result;
  }

  public void setResult(Object result) {
    this.result = result;
  }
}
