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

public interface ConstantVisitor extends Visitor {

  void caseBooleanConstant(@NonNull BooleanConstant constant);

  void caseDoubleConstant(@NonNull DoubleConstant constant);

  void caseFloatConstant(@NonNull FloatConstant constant);

  void caseIntConstant(@NonNull IntConstant constant);

  void caseLongConstant(@NonNull LongConstant constant);

  void caseNullConstant(@NonNull NullConstant constant);

  void caseStringConstant(@NonNull StringConstant constant);

  void caseEnumConstant(@NonNull EnumConstant constant);

  void caseClassConstant(@NonNull ClassConstant constant);

  void caseMethodHandle(@NonNull MethodHandle handle);

  void caseMethodType(@NonNull MethodType methodType);

  void defaultCaseConstant(@NonNull Constant constant);
}
