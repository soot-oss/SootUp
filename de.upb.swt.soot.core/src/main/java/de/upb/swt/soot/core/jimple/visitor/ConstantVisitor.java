package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-1999 Etienne Gagnon
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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.jimple.common.constant.BooleanConstant;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.DoubleConstant;
import de.upb.swt.soot.core.jimple.common.constant.FloatConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.MethodHandle;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;

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

  void defaultCase(Object object);
}
