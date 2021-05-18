package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Etienne Gagnon, Linghui Luo, Christian Brüggemann
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

public interface ConstantVisitor extends Visitor {
  void caseBooleanConstant(BooleanConstant v);

  void caseDoubleConstant(DoubleConstant v);

  void caseFloatConstant(FloatConstant v);

  void caseIntConstant(IntConstant v);

  void caseLongConstant(LongConstant v);

  void caseNullConstant(NullConstant v);

  void caseStringConstant(StringConstant v);

  void caseClassConstant(ClassConstant v);

  void caseMethodHandle(MethodHandle handle);

  void caseMethodType(MethodType methodType);

  void defaultCase(Constant constant);
}
