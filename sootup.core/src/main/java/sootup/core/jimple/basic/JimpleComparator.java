package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 2018-2020 Christian Brüggemann, Markus Schmidt and others
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

import java.util.Iterator;
import java.util.List;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.expr.*;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.jimple.javabytecode.stmt.*;

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
    Local local = (Local) o;
    return obj.getName().equals(local.getName()) && obj.getType().equals(local.getType());
  }

  public boolean caseBlock(BasicBlock<?> block, Object o) {
    if (!(o instanceof BasicBlock<?>)) {
      return false;
    }
    BasicBlock<?> obj = (BasicBlock<?>) o;
    return caseStmt(block.getHead(), obj.getHead()) && caseStmt(block.getTail(), obj.getTail());
  }

  public boolean caseStmt(Stmt stmt, Object o) {
    if (!(o instanceof Stmt)) {
      return false;
    } else if (stmt instanceof JBreakpointStmt) {
      return caseBreakpointStmt((JBreakpointStmt) stmt, o);
    } else if (stmt instanceof JInvokeStmt) {
      return caseInvokeStmt((JInvokeStmt) stmt, o);
    } else if (stmt instanceof JAssignStmt) {
      return caseAssignStmt((JAssignStmt) stmt, o);
    } else if (stmt instanceof JIdentityStmt) {
      return caseIdentityStmt((JIdentityStmt) stmt, o);
    } else if (stmt instanceof JEnterMonitorStmt) {
      return caseEnterMonitorStmt((JEnterMonitorStmt) stmt, o);
    } else if (stmt instanceof JExitMonitorStmt) {
      return caseExitMonitorStmt((JExitMonitorStmt) stmt, o);
    } else if (stmt instanceof JGotoStmt) {
      return caseGotoStmt((JGotoStmt) stmt, o);
    } else if (stmt instanceof JIfStmt) {
      return caseIfStmt((JIfStmt) stmt, o);
    } else if (stmt instanceof JSwitchStmt) {
      return caseSwitchStmt((JSwitchStmt) stmt, o);
    } else if (stmt instanceof JNopStmt) {
      return caseNopStmt((JNopStmt) stmt, o);
    } else if (stmt instanceof JRetStmt) {
      return caseRetStmt((JRetStmt) stmt, o);
    } else if (stmt instanceof JReturnStmt) {
      return caseReturnStmt((JReturnStmt) stmt, o);
    } else if (stmt instanceof JReturnVoidStmt) {
      return caseReturnVoidStmt((JReturnVoidStmt) stmt, o);
    } else if (stmt instanceof JThrowStmt) {
      return caseThrowStmt((JThrowStmt) stmt, o);
    } else {
      return false;
    }
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
    return (o instanceof JGotoStmt);
  }

  public boolean caseIfStmt(JIfStmt stmt, Object o) {
    if (!(o instanceof JIfStmt)) {
      return false;
    }
    JIfStmt ifStmt = (JIfStmt) o;
    return stmt.getCondition().equivTo(ifStmt.getCondition(), this);
  }

  /**
   * assumes that different sequence of (otherwise equivalent) cases means values are not considered
   * equivalent
   */
  public boolean caseSwitchStmt(JSwitchStmt stmt, Object o) {
    if (!(o instanceof JSwitchStmt)) {
      return false;
    }
    JSwitchStmt otherSwitchStmt = (JSwitchStmt) o;

    if (stmt.getKey() != otherSwitchStmt.getKey()) {
      return false;
    }

    if (stmt.getValueCount() != otherSwitchStmt.getValueCount()) {
      return false;
    }

    Iterator<IntConstant> valueIterator = stmt.getValues().iterator();
    for (IntConstant valuesOther : otherSwitchStmt.getValues()) {
      if (!valuesOther.equivTo(valueIterator.next(), this)) {
        return false;
      }
    }

    return true;
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

  public boolean caseThrowStmt(JThrowStmt stmt, Object o) {
    if (!(o instanceof JThrowStmt)) {
      return false;
    }
    return stmt.getOp().equivTo(((AbstractOpStmt) o).getOp(), this);
  }

  public boolean caseAbstractBinopExpr(AbstractBinopExpr obj, Object o) {
    if (o instanceof AbstractBinopExpr) {
      AbstractBinopExpr abe = (AbstractBinopExpr) o;

      // JEqExpr (1=2 <=> 2=1)
      if (obj instanceof JEqExpr && o instanceof JEqExpr) {
        JEqExpr jeq1 = (JEqExpr) abe;
        JEqExpr jeq2 = (JEqExpr) obj;

        return (jeq1.getOp1().equivTo(jeq2.getOp1()) && jeq1.getOp2().equivTo(jeq2.getOp2())
                || (jeq1.getOp1().equivTo(jeq2.getOp2()) && jeq1.getOp2().equivTo(jeq2.getOp1())))
            && jeq1.getSymbol().equals(jeq2.getSymbol());
      }

      // JGtExpr/JLtExpr (1<2 <=> 2>1)
      if (obj instanceof JGtExpr && o instanceof JLtExpr) {
        JGtExpr jgt = (JGtExpr) obj;
        JLtExpr jlt = (JLtExpr) o;

        return (jgt.getOp1().equivTo(jlt.getOp2()) && jgt.getOp2().equivTo(jlt.getOp1()));
      } else if (o instanceof JGtExpr && obj instanceof JLtExpr) {
        JGtExpr jgt = (JGtExpr) o;
        JLtExpr jlt = (JLtExpr) obj;

        return (jgt.getOp1().equivTo(jlt.getOp2()) && jgt.getOp2().equivTo(jlt.getOp1()));
      }

      // JGeExpr/JLeExpr (1<=2 <=> 2=>1)
      if (obj instanceof JGeExpr && o instanceof JLeExpr) {
        JGeExpr jgt = (JGeExpr) obj;
        JLeExpr jlt = (JLeExpr) o;

        return (jgt.getOp1().equivTo(jlt.getOp2()) && jgt.getOp2().equivTo(jlt.getOp1()));
      } else if (o instanceof JGeExpr && obj instanceof JLeExpr) {
        JGeExpr jgt = (JGeExpr) o;
        JLeExpr jlt = (JLeExpr) obj;

        return (jgt.getOp1().equivTo(jlt.getOp2()) && jgt.getOp2().equivTo(jlt.getOp1()));
      }

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
    if (!(v.getMethodSignature().equals(ie.getMethodSignature())
        && (v.getArgCount() == ie.getArgCount()))) {
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
    if (!(v.getBootstrapMethodSignature().equals(ie.getBootstrapMethodSignature())
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
    if (!(v.getMethodSignature().equals(ie.getMethodSignature())
        && v.getArgCount() == ie.getArgCount())) {
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

  public boolean caseJPhiExpr(JPhiExpr v, Object o) {
    if (!(o instanceof JPhiExpr)) {
      return false;
    }
    JPhiExpr ae = (JPhiExpr) o;
    for (int i = 0; i < v.getArgsSize(); i++) {
      if (!v.getArg(i).equivTo(ae.getArg(i), this)) {
        return false;
      }
    }
    List<BasicBlock<?>> blocksV = v.getBlocks();
    List<BasicBlock<?>> blocksAe = ae.getBlocks();
    for (int i = 0; i < v.getArgsSize(); i++) {
      if (!caseBlock(blocksV.get(i), blocksAe.get(i))) {
        return false;
      }
    }
    return true;
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
        && obj.getMethodSignature().equals(ie.getMethodSignature())
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
    return fr.getFieldSignature().equals(obj.getFieldSignature())
        && obj.getBase().equivTo(fr.getBase(), this);
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
    return obj.getFieldSignature().equals(((JStaticFieldRef) o).getFieldSignature());
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
