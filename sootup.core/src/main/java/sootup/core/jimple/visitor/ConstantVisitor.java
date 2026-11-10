package sootup.core.jimple.visitor;

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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.constant.*;

/** Visitor interface for constant values in Jimple IR. */
public interface ConstantVisitor extends Visitor {

  /** Visits a boolean constant. */
  void caseBooleanConstant(@NonNull BooleanConstant constant);

  /** Visits a double constant. */
  void caseDoubleConstant(@NonNull DoubleConstant constant);

  /** Visits a float constant. */
  void caseFloatConstant(@NonNull FloatConstant constant);

  /** Visits an int constant. */
  void caseIntConstant(@NonNull IntConstant constant);

  /** Visits a long constant. */
  void caseLongConstant(@NonNull LongConstant constant);

  /** Visits a null constant. */
  void caseNullConstant(@NonNull NullConstant constant);

  /** Visits a string constant. */
  void caseStringConstant(@NonNull StringConstant constant);

  /** Visits an enum constant. */
  void caseEnumConstant(@NonNull EnumConstant constant);

  /** Visits a class constant. */
  void caseClassConstant(@NonNull ClassConstant constant);

  /** Visits a method handle constant. */
  void caseMethodHandle(@NonNull MethodHandle handle);

  /** Visits a method type constant. */
  void caseMethodType(@NonNull MethodType methodType);

  /** Called for any constant not handled by a more specific case method. */
  void defaultCaseConstant(@NonNull Constant constant);
}
