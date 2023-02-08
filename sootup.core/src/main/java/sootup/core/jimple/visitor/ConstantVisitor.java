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

import javax.annotation.Nonnull;
import sootup.core.jimple.common.constant.*;

public interface ConstantVisitor extends Visitor {

  void caseBooleanConstant(@Nonnull BooleanConstant constant);

  void caseDoubleConstant(@Nonnull DoubleConstant constant);

  void caseFloatConstant(@Nonnull FloatConstant constant);

  void caseIntConstant(@Nonnull IntConstant constant);

  void caseLongConstant(@Nonnull LongConstant constant);

  void caseNullConstant(@Nonnull NullConstant constant);

  void caseStringConstant(@Nonnull StringConstant constant);

  void caseEnumConstant(@Nonnull EnumConstant constant);

  void caseClassConstant(@Nonnull ClassConstant constant);

  void caseMethodHandle(@Nonnull MethodHandle handle);

  void caseMethodType(@Nonnull MethodType methodType);

  void defaultCaseConstant(@Nonnull Constant constant);
}
