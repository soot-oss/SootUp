/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 213.12.2018 Markus Schmidt
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

package de.upb.soot.jimple.basic;

import de.upb.soot.jimple.common.constant.Constant;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.expr.AbstractBinopExpr;
import de.upb.soot.jimple.common.expr.AbstractInstanceInvokeExpr;
import de.upb.soot.jimple.common.expr.JCastExpr;
import de.upb.soot.jimple.common.expr.JDynamicInvokeExpr;
import de.upb.soot.jimple.common.expr.JInstanceOfExpr;
import de.upb.soot.jimple.common.expr.JInterfaceInvokeExpr;
import de.upb.soot.jimple.common.expr.JLengthExpr;
import de.upb.soot.jimple.common.expr.JNegExpr;
import de.upb.soot.jimple.common.expr.JNewArrayExpr;
import de.upb.soot.jimple.common.expr.JNewExpr;
import de.upb.soot.jimple.common.expr.JNewMultiArrayExpr;
import de.upb.soot.jimple.common.expr.JSpecialInvokeExpr;
import de.upb.soot.jimple.common.expr.JStaticInvokeExpr;
import de.upb.soot.jimple.common.expr.JVirtualInvokeExpr;
import de.upb.soot.jimple.common.ref.JArrayRef;
import de.upb.soot.jimple.common.ref.JCaughtExceptionRef;
import de.upb.soot.jimple.common.ref.JInstanceFieldRef;
import de.upb.soot.jimple.common.ref.JParameterRef;
import de.upb.soot.jimple.common.ref.JStaticFieldRef;
import de.upb.soot.jimple.common.ref.JThisRef;
import de.upb.soot.jimple.common.stmt.AbstractOpStmt;
import de.upb.soot.jimple.common.stmt.AbstractSwitchStmt;
import de.upb.soot.jimple.common.stmt.JAssignStmt;
import de.upb.soot.jimple.common.stmt.JGotoStmt;
import de.upb.soot.jimple.common.stmt.JIdentityStmt;
import de.upb.soot.jimple.common.stmt.JIfStmt;
import de.upb.soot.jimple.common.stmt.JInvokeStmt;
import de.upb.soot.jimple.common.stmt.JNopStmt;
import de.upb.soot.jimple.common.stmt.JReturnStmt;
import de.upb.soot.jimple.common.stmt.JReturnVoidStmt;
import de.upb.soot.jimple.common.stmt.JThrowStmt;
import de.upb.soot.jimple.javabytecode.stmt.JBreakpointStmt;
import de.upb.soot.jimple.javabytecode.stmt.JEnterMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JExitMonitorStmt;
import de.upb.soot.jimple.javabytecode.stmt.JLookupSwitchStmt;
import de.upb.soot.jimple.javabytecode.stmt.JRetStmt;
import de.upb.soot.jimple.javabytecode.stmt.JTableSwitchStmt;
import java.util.Iterator;

/**
 * This class contains the equivalence implementations for the individual {@link
 * EquivTo#equivTo(Object)} methods of the Jimple IR. You can use it as your base class if your use
 * case needs an adjustment for checking structural equivalence. This follows a contract weaker than
 * {@link Object#equals(Object)}:
 *
 * <p>{@code X x = ...; Y y = ...; x.equivTo(y);} will check whether {@code y} is an instance of
 * {@code X} and whether the properties known to {@code X} are equal in both objects.
 *
 * <ul>
 *   <li>It is <i>reflexive</i>: for any non-null reference value {@code x}, {@code x.equivTo(x)}
 *       should return {@code true}.
 *   <li>In contrast to {@link Object#equals(Object)} it is <b>not</b> necessarily <i>symmetric</i>:
 *       Consider reference values {@code Foo x} and {@code Bar y} where {@code class Bar extends
 *       Foo}. If {@code x.equivTo(y)} returns {@code true}, it is still valid for {@code
 *       y.equivTo(x)} to return {@code false}. This is because {@code x.equivTo(y)} will only
 *       compare the properties defined in {@code Foo}. If {@code Bar} has added any other
 *       properties, it will also take these into consideration for the comparison. Since {@code
 *       Foo} does not contain them, {@code y.equivTo(x)} will return {@code false} in this case.
 *   <li>In contrast to {@link Object#equals(Object)} it is <b>not</b> necessarily
 *       <i>transitive</i>. This is because it is reflexive, but not necessarily symmetric. This
 *       means that when {@code x.equivTo(y) == true} and {@code y.equivTo(z) == true}, this does
 *       not imply {@code x.equivTo(z) == true}. A trivial example showing this is when {@code x}
 *       refers to the same object as {@code z}, since {@code equivTo} is not always symmetric.
 *   <li>It is <i>consistent</i>: for any non-null reference values {@code x} and {@code y},
 *       multiple invocations of {@code x.equivTo(y)} consistently return {@code true} or
 *       consistently return {@code false}, provided no information used in {@code equivTo}
 *       comparisons on the objects is modified.
 *   <li>For any non-null reference value {@code x}, {@code x.equivTo(null)} should return {@code
 *       false}.
 * </ul>
 *
 * <p>
 *
 * @author Markus Schmidt
 */
public class JimpleComparator {

  private static final JimpleComparator INSTANCE = new JimpleComparator();

  /**
   * Returns the default {@link JimpleComparator}. You may customize the behavior by extending
   * {@link JimpleComparator} and overriding methods.
   */
  public static JimpleComparator getInstance() {
    return INSTANCE;
  }

  public boolean caseLocal(Local obj, Object o) {
    if (!(o instanceof Local)) {
      return false;
    }
    return obj.equivHashCode() == ((Local) o).equivHashCode();
  }

  public boolean caseBreakpointStmt(JBreakpointStmt stmt, Object o) {
    return (o instanceof JBreakpointStmt);
  }

  public boolean caseInvokeStmt(JInvokeStmt stmt, Object o) {
    return (o instanceof JInvokeStmt)
        && stmt.getInvokeExpr().equivTo(((JInvokeStmt) o).getInvokeExpr(), this);
  }

  public boolean caseAssignStmt(JAssignStmt stmt, Object o) {
    if (!(o instanceof JAssignStmt)) {
      return false;
    }
    JAssignStmt jas = (JAssignStmt) o;
    return stmt.getLeftOp().equivTo(jas.getLeftOp(), this)
        && stmt.getRightOp().equivTo(jas.getRightOp(), this);
  }

  public boolean caseIdentityStmt(JIdentityStmt stmt, Object o) {
    if (!(o instanceof JIdentityStmt)) {
      return false;
    }
    JIdentityStmt identityStmt = (JIdentityStmt) o;
    return stmt.getLeftOp().equivTo(identityStmt.getLeftOp(), this)
        && stmt.getRightOp().equivTo(identityStmt.getRightOp(), this);
  }

  public boolean caseEnterMonitorStmt(JEnterMonitorStmt stmt, Object o) {
    if (!(o instanceof JEnterMonitorStmt)) {
      return false;
    }
    return stmt.getOp().equivTo(((AbstractOpStmt) o).getOp(), this);
  }

  public boolean caseExitMonitorStmt(JExitMonitorStmt stmt, Object o) {
    if (!(o instanceof JExitMonitorStmt)) {
      return false;
    }
    return stmt.getOp().equivTo(((AbstractOpStmt) o).getOp(), this);
  }

  public boolean caseGotoStmt(JGotoStmt stmt, Object o) {
    return (o instanceof JGotoStmt) && stmt.getTarget().equivTo(((JGotoStmt) o).getTarget(), this);
  }

  public boolean caseIfStmt(JIfStmt stmt, Object o) {
    if (!(o instanceof JIfStmt)) {
      return false;
    }
    JIfStmt ifStmt = (JIfStmt) o;
    return stmt.getCondition().equivTo(ifStmt.getCondition(), this)
        && stmt.getTarget().equivTo(ifStmt.getTarget(), this);
  }

  protected boolean caseAbstractSwitchStmt(AbstractSwitchStmt obj, AbstractSwitchStmt o) {
    if (obj.getKey() != o.getKey() || obj.getDefaultTarget() != o.getDefaultTarget()) {
      return false;
    }
    if (obj.getTargetCount() != o.getTargetCount()) {
      return false;
    }
    for (int i = obj.getTargetCount() - 1; i >= 0; i--) {
      if (!obj.getTarget(i).equivTo(o.getTarget(i), this)) {
        return false;
      }
    }
    return true;
  }

  public boolean caseLookupSwitchStmt(JLookupSwitchStmt stmt, Object o) {
    if (!(o instanceof JLookupSwitchStmt)) {
      return false;
    }

    JLookupSwitchStmt lookupSwitchStmt = (JLookupSwitchStmt) o;
    if (stmt.getLookupValueCount() != lookupSwitchStmt.getLookupValueCount()) {
      return false;
    }
    Iterator<IntConstant> lvIterator = stmt.getLookupValues().iterator();
    for (IntConstant lvOther : lookupSwitchStmt.getLookupValues()) {
      if (!lvOther.equivTo(lvIterator.next(), this)) {
        return false;
      }
    }
    return caseAbstractSwitchStmt(stmt, lookupSwitchStmt);
  }

  public boolean caseNopStmt(JNopStmt stmt, Object o) {
    return o instanceof JNopStmt;
  }

  public boolean caseRetStmt(JRetStmt stmt, Object o) {
    if (!(o instanceof JRetStmt)) {
      return false;
    }
    return stmt.getStmtAddress().equivTo(((JRetStmt) o).getStmtAddress(), this);
  }

  public boolean caseReturnStmt(JReturnStmt stmt, Object o) {
    if (!(o instanceof JReturnStmt)) {
      return false;
    }
    return stmt.getOp().equivTo(((AbstractOpStmt) o).getOp(), this);
  }

  public boolean caseReturnVoidStmt(JReturnVoidStmt stmt, Object o) {
    return (o instanceof JReturnVoidStmt);
  }

  public boolean caseTableSwitchStmt(JTableSwitchStmt stmt, Object o) {
    if (!(o instanceof JTableSwitchStmt)) {
      return false;
    }
    JTableSwitchStmt tableSwitchStmt = (JTableSwitchStmt) o;
    if (stmt.getLowIndex() != tableSwitchStmt.getLowIndex()
        || stmt.getHighIndex() != tableSwitchStmt.getHighIndex()) {
      return false;
    }
    return caseAbstractSwitchStmt(stmt, tableSwitchStmt);
  }

  public boolean caseThrowStmt(JThrowStmt stmt, Object o) {
    if (!(o instanceof JThrowStmt)) {
      return false;
    }
    return stmt.getOp().equivTo(((AbstractOpStmt) o).getOp(), this);
  }

  public boolean caseAbstractBinopExpr(AbstractBinopExpr obj, Object o) {
    if (o instanceof AbstractBinopExpr) {
      AbstractBinopExpr abe = (AbstractBinopExpr) o;
      return obj.getOp1().equivTo(abe.getOp1(), this)
          && obj.getOp2().equivTo(abe.getOp2(), this)
          && obj.getSymbol().equals(abe.getSymbol());
    }
    return false;
  }

  public boolean caseStaticInvokeExpr(JStaticInvokeExpr v, Object o) {
    if (!(o instanceof JStaticInvokeExpr)) {
      return false;
    }
    JStaticInvokeExpr ie = (JStaticInvokeExpr) o;
    if (!(v.getMethod().equals(ie.getMethod()) && (v.getArgCount() == ie.getArgCount()))) {
      return false;
    }
    for (int i = v.getArgCount() - 1; i >= 0; i--) {
      if (!(v.getArg(i).equivTo(ie.getArg(i), this))) {
        return false;
      }
    }
    return true;
  }

  public boolean caseDynamicInvokeExpr(JDynamicInvokeExpr v, Object o) {
    if (!(o instanceof JDynamicInvokeExpr)) {
      return false;
    }
    JDynamicInvokeExpr ie = (JDynamicInvokeExpr) o;
    if (!(v.getBootstrapMethod().equals(ie.getBootstrapMethod())
        && v.getBootstrapArgCount() == ie.getBootstrapArgCount())) {
      return false;
    }
    Value element;
    for (int i = v.getBootstrapArgCount() - 1; i >= 0; i--) {
      element = v.getBootstrapArg(i);
      if (!(element.equivTo(ie.getBootstrapArg(i), this))) {
        return false;
      }
    }
    if (!(v.getMethod().equals(ie.getMethod()) && v.getArgCount() == ie.getArgCount())) {
      return false;
    }
    for (int i = v.getArgCount() - 1; i >= 0; i--) {
      element = v.getArg(i);
      if (!(element.equivTo(ie.getArg(i), this))) {
        return false;
      }
    }
    return true;
  }

  public boolean caseCastExpr(JCastExpr v, Object o) {
    if (!(o instanceof JCastExpr)) {
      return false;
    }
    JCastExpr ace = (JCastExpr) o;
    return v.getOp().equivTo(ace.getOp(), this) && v.getType().equals(ace.getType());
  }

  public boolean caseInstanceOfExpr(JInstanceOfExpr v, Object o) {
    if (!(o instanceof JInstanceOfExpr)) {
      return false;
    }
    JInstanceOfExpr aie = (JInstanceOfExpr) o;
    return v.getOp().equivTo(aie.getOp(), this) && v.getCheckType().equals(aie.getCheckType());
  }

  public boolean caseNewArrayExpr(JNewArrayExpr v, Object o) {
    if (!(o instanceof JNewArrayExpr)) {
      return false;
    }
    JNewArrayExpr ae = (JNewArrayExpr) o;
    return v.getBaseType().equals(ae.getBaseType()) && v.getSize().equivTo(ae.getSize(), this);
  }

  public boolean caseNewMultiArrayExpr(JNewMultiArrayExpr v, Object o) {
    if (!(o instanceof JNewMultiArrayExpr)) {
      return false;
    }
    JNewMultiArrayExpr ae = (JNewMultiArrayExpr) o;
    return v.getBaseType().equals(ae.getBaseType()) && v.getSizeCount() == ae.getSizeCount();
  }

  public boolean caseNewExpr(JNewExpr v, Object o) {
    if (!(o instanceof JNewExpr)) {
      return false;
    }
    JNewExpr ae = (JNewExpr) o;
    return v.getType().equals(ae.getType());
  }

  public boolean caseLengthExpr(JLengthExpr v, Object o) {
    if (!(o instanceof JLengthExpr)) {
      return false;
    }
    return v.getOp().equivTo(((JLengthExpr) o).getOp(), this);
  }

  public boolean caseNegExpr(JNegExpr v, Object o) {
    if (!(o instanceof JNegExpr)) {
      return false;
    }
    return v.getOp().equivTo(((JNegExpr) o).getOp(), this);
  }

  protected boolean caseAbstractInstanceInvokeExpr(AbstractInstanceInvokeExpr obj, Object o) {
    if (!(o instanceof AbstractInstanceInvokeExpr)) {
      return false;
    }
    AbstractInstanceInvokeExpr ie = (AbstractInstanceInvokeExpr) o;
    if (!(obj.getBase().equivTo(ie.getBase(), this)
        && obj.getMethod().equals(ie.getMethod())
        && obj.getArgCount() == ie.getArgCount())) {
      return false;
    }
    for (int i = obj.getArgCount() - 1; i >= 0; i--) {
      if (!obj.getArg(i).equivTo(ie.getArg(i), this)) {
        return false;
      }
    }
    return true;
  }

  public boolean caseInterfaceInvokeExpr(JInterfaceInvokeExpr obj, Object o) {
    if (!(o instanceof JInterfaceInvokeExpr)) {
      return false;
    }
    return caseAbstractInstanceInvokeExpr(obj, o);
  }

  public boolean caseSpecialInvokeExpr(JSpecialInvokeExpr obj, Object o) {
    if (!(o instanceof JSpecialInvokeExpr)) {
      return false;
    }
    return caseAbstractInstanceInvokeExpr(obj, o);
  }

  public boolean caseVirtualInvokeExpr(JVirtualInvokeExpr obj, Object o) {
    if (!(o instanceof JVirtualInvokeExpr)) {
      return false;
    }
    return caseAbstractInstanceInvokeExpr(obj, o);
  }

  public boolean caseArrayRef(JArrayRef obj, Object o) {
    if (!(o instanceof JArrayRef)) {
      return false;
    }
    return obj.getBase().equivTo(((JArrayRef) o).getBase(), this)
        && obj.getIndex().equivTo(((JArrayRef) o).getIndex(), this);
  }

  public boolean caseCaughtException(JCaughtExceptionRef obj, Object o) {
    return o instanceof JCaughtExceptionRef;
  }

  public boolean caseInstanceFieldRef(JInstanceFieldRef obj, Object o) {
    if (!(o instanceof JInstanceFieldRef)) {
      return false;
    }
    JInstanceFieldRef fr = (JInstanceFieldRef) o;
    return fr.getField().equals(obj.getField()) && obj.getBase().equivTo(fr.getBase(), this);
  }

  public boolean caseParameterRef(JParameterRef obj, Object o) {
    if (!(o instanceof JParameterRef)) {
      return false;
    }
    return obj.getIndex() == ((JParameterRef) o).getIndex()
        && obj.getType().equals(((JParameterRef) o).getType());
  }

  public boolean caseStaticFieldRef(JStaticFieldRef obj, Object o) {
    if (!(o instanceof JStaticFieldRef)) {
      return false;
    }
    return obj.getField().equals(((JStaticFieldRef) o).getField());
  }

  public boolean caseThisRef(JThisRef obj, Object o) {
    if (!(o instanceof JThisRef)) {
      return false;
    }
    return obj.getType().equals(((JThisRef) o).getType());
  }

  public boolean caseConstant(Constant constant, Object o) {
    return constant.equals(o);
  }
}
