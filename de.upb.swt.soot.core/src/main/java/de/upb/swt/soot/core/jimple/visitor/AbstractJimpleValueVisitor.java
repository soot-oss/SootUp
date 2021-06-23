package de.upb.swt.soot.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Kadiray Karakaya and others
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

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;

public abstract class AbstractJimpleValueVisitor<T> extends AbstractConstantVisitor<T>
    implements JimpleValueRefVisitor {
  @Override
  public void caseArrayRef(JArrayRef v) {
    defaultCaseRef(v);
  }

  @Override
  public void caseAddExpr(JAddExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseAndExpr(JAndExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseCmpExpr(JCmpExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseCmpgExpr(JCmpgExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseCmplExpr(JCmplExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseDivExpr(JDivExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseEqExpr(JEqExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseGeExpr(JGeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseGtExpr(JGtExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseLeExpr(JLeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseLtExpr(JLtExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseMulExpr(JMulExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseNeExpr(JNeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseOrExpr(JOrExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseRemExpr(JRemExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseShlExpr(JShlExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseShrExpr(JShrExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseSubExpr(JSubExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseUshrExpr(JUshrExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseXorExpr(JXorExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseSpecialInvokeExpr(JSpecialInvokeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseStaticInvokeExpr(JStaticInvokeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseVirtualInvokeExpr(JVirtualInvokeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseDynamicInvokeExpr(JDynamicInvokeExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseCastExpr(JCastExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseInstanceOfExpr(JInstanceOfExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseNewArrayExpr(JNewArrayExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseNewMultiArrayExpr(JNewMultiArrayExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseNewExpr(JNewExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseLengthExpr(JLengthExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseNegExpr(JNegExpr v) {
    defaultCaseExpr(v);
  }

  @Override
  public void caseInstanceFieldRef(JInstanceFieldRef v) {
    defaultCaseRef(v);
  }

  @Override
  public void caseLocal(Local v) {
    defaultCaseValue(v);
  }

  @Override
  public void caseParameterRef(JParameterRef v) {
    defaultCaseRef(v);
  }

  @Override
  public void caseCaughtExceptionRef(JCaughtExceptionRef v) {
    defaultCaseRef(v);
  }

  @Override
  public void caseThisRef(JThisRef v) {
    defaultCaseRef(v);
  }

  @Override
  public void caseStaticFieldRef(JStaticFieldRef v) {
    defaultCaseRef(v);
  }
}
