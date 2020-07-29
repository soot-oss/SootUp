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

import de.upb.swt.soot.core.jimple.common.expr.*;

public interface ExprVisitor extends Visitor {
  void caseAddExpr(JAddExpr v);

  void caseAndExpr(JAndExpr v);

  void caseCmpExpr(JCmpExpr v);

  void caseCmpgExpr(JCmpgExpr v);

  void caseCmplExpr(JCmplExpr v);

  void caseDivExpr(JDivExpr v);

  void caseEqExpr(JEqExpr v);

  void caseNeExpr(JNeExpr v);

  void caseGeExpr(JGeExpr v);

  void caseGtExpr(JGtExpr v);

  void caseLeExpr(JLeExpr v);

  void caseLtExpr(JLtExpr v);

  void caseMulExpr(JMulExpr v);

  void caseOrExpr(JOrExpr v);

  void caseRemExpr(JRemExpr v);

  void caseShlExpr(JShlExpr v);

  void caseShrExpr(JShrExpr v);

  void caseUshrExpr(JUshrExpr v);

  void caseSubExpr(JSubExpr v);

  void caseXorExpr(JXorExpr v);

  void caseSpecialInvokeExpr(JSpecialInvokeExpr v);

  void caseVirtualInvokeExpr(JVirtualInvokeExpr v);

  void caseInterfaceInvokeExpr(JInterfaceInvokeExpr v);

  void caseStaticInvokeExpr(JStaticInvokeExpr v);

  void caseDynamicInvokeExpr(JDynamicInvokeExpr v);

  void caseCastExpr(JCastExpr v);

  void caseInstanceOfExpr(JInstanceOfExpr v);

  void caseNewArrayExpr(JNewArrayExpr v);

  void caseNewMultiArrayExpr(JNewMultiArrayExpr v);

  void caseNewExpr(JNewExpr v);

  void caseLengthExpr(JLengthExpr v);

  void caseNegExpr(JNegExpr v);

  void defaultCase(Object obj);
}
