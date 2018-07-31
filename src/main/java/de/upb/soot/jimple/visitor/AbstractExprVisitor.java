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

package de.upb.soot.jimple.visitor;

import de.upb.soot.jimple.common.expr.AbstractInterfaceInvokeExpr;
import de.upb.soot.jimple.common.expr.AbstractSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.AbstractVirtualInvokeExpr;
import de.upb.soot.jimple.common.expr.JAddExpr;
import de.upb.soot.jimple.common.expr.JAndExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JCmpExpr;
import de.upb.soot.jimple.common.expr.JCmpgExpr;
import de.upb.soot.jimple.common.expr.JCmplExpr;
import de.upb.soot.jimple.common.expr.JDivExpr;
import de.upb.soot.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.soot.jimple.common.expr.JEqExpr;
import de.upb.soot.jimple.common.expr.JGeExpr;
import de.upb.soot.jimple.common.expr.JGtExpr;
import de.upb.soot.jimple.common.expr.JInstanceOfExpr;
import de.upb.soot.jimple.common.expr.JLeExpr;
import de.upb.soot.jimple.common.expr.JLengthExpr;
import de.upb.soot.jimple.common.expr.JLtExpr;
import de.upb.soot.jimple.common.expr.JMulExpr;
import de.upb.soot.jimple.common.expr.JNeExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.expr.JNewArrayExpr;
import de.upb.soot.jimple.common.expr.JNewExpr;
import de.upb.soot.jimple.common.expr.JNewMultiArrayExpr;
import de.upb.soot.jimple.common.expr.JOrExpr;
import de.upb.soot.jimple.common.expr.JRemExpr;
import de.upb.soot.jimple.common.expr.JShlExpr;
import de.upb.soot.jimple.common.expr.JShrExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.jimple.common.expr.JSubExpr;
import de.upb.soot.jimple.common.expr.JUshrExpr;
import de.upb.soot.jimple.common.expr.JXorExpr;

public abstract class AbstractExprVisitor implements IExprVisitor {
  Object result;

  @Override
  public void caseAddExpr(JAddExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseAndExpr(JAndExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmpExpr(JCmpExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmpgExpr(JCmpgExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCmplExpr(JCmplExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseDivExpr(JDivExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseEqExpr(JEqExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNeExpr(JNeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseGeExpr(JGeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseGtExpr(JGtExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLeExpr(JLeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLtExpr(JLtExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseMulExpr(JMulExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseOrExpr(JOrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseRemExpr(JRemExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseShlExpr(JShlExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseShrExpr(JShrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseUshrExpr(JUshrExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseSubExpr(JSubExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseXorExpr(JXorExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseInterfaceInvokeExpr(AbstractInterfaceInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseSpecialInvokeExpr(AbstractSpecialInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseVirtualInvokeExpr(AbstractVirtualInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseCastExpr(JCastExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseInstanceOfExpr(JInstanceOfExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNewExpr(JNewExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseLengthExpr(JLengthExpr v) {
    defaultCase(v);
  }

  @Override
  public void caseNegExpr(JNegExpr v) {
    defaultCase(v);
  }

  @Override
  public void defaultCase(Object obj) {
  }

  public void setResult(Object result) {
    this.result = result;
  }

  public Object getResult() {
    return result;
  }
}
