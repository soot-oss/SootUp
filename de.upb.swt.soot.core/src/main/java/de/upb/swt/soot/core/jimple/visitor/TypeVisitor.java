package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Christian Brüggemann
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

import com.google.common.graph.ElementOrder.Type;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.VoidType;

public interface TypeVisitor {

  void caseBooleanType(PrimitiveType t);

  void caseByteType(PrimitiveType t);

  void caseCharType(PrimitiveType t);

  void caseShortType(PrimitiveType t);

  void caseIntType(PrimitiveType t);

  void caseLongType(PrimitiveType t);

  void caseDoubleType(PrimitiveType t);

  void caseFloatType(PrimitiveType t);

  void caseArrayType(PrimitiveType t);

  void caseRefType(ClassType t);

  void caseNullType(NullType t);

  void caseUnknownType(/*UnknownType t*/ );

  void caseVoidType(VoidType t);

  void caseDefault(Type t);
}
