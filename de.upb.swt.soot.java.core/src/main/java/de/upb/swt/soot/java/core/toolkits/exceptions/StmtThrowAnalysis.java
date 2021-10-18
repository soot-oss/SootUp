package de.upb.swt.soot.java.core.toolkits.exceptions;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2003 John Jorgensen
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

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.constant.LongConstant;
import de.upb.swt.soot.core.jimple.common.constant.NullConstant;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;
import javafx.scene.Scene;

import java.util.*;

/**
 * A {@link ThrowAnalysis} which returns the set of runtime exceptions and errors that might be thrown by the bytecode
 * instructions represented by a unit, as indicated by the Java Virtual Machine specification. I.e. this analysis is based
 * entirely on the &ldquo;opcode&rdquo; of the unit, the types of its arguments, and the values of constant arguments.
 *
 * <p>
 * The <code>mightThrow</code> methods could be declared static. They are left virtual to facilitate testing. For example, to
 * verify that the expressions in a method call are actually being examined, a test case can override the
 * mightThrow(SootMethod) with an implementation which returns the empty set instead of all possible exceptions.
 */
public class StmtThrowAnalysis extends AbstractThrowAnalysis {

  protected final ThrowableSet.Manager mgr = ThrowableSet.Manager.v();

  // Cache the response to mightThrowImplicitly():
  private final ThrowableSet implicitThrowExceptions = ThrowableSet.Manager.v().VM_ERRORS
      .add(ThrowableSet.Manager.v().NULL_POINTER_EXCEPTION).add(ThrowableSet.Manager.v().ILLEGAL_MONITOR_STATE_EXCEPTION);

  /**
   * Constructs a <code>UnitThrowAnalysis</code> for inclusion in Soot's global variable manager, {@link G}.
   *
   * @param g
   *          guarantees that the constructor may only be called from {@link Singletons}.
   */
  public StmtThrowAnalysis() {
    this(false);
  }


  /**
   * Returns the single instance of <code>UnitThrowAnalysis</code>.
   *
   * @return Soot's <code>UnitThrowAnalysis</code>.
   */

  protected final boolean isInterproc;

  protected StmtThrowAnalysis(boolean isInterproc) {
    this.isInterproc = isInterproc;
  }

  public static StmtThrowAnalysis interproceduralAnalysis = null;

  public static StmtThrowAnalysis interproc() {
    if (interproceduralAnalysis == null) {
      interproceduralAnalysis = new StmtThrowAnalysis(true);
    }
    return interproceduralAnalysis;
  }

  protected ThrowableSet defaultResult() {
    return mgr.VM_ERRORS;
  }

  protected UnitSwitch unitSwitch(SootMethod sm) {
    return new UnitSwitch(sm);
  }

  protected ValueSwitch valueSwitch() {
    return new ValueSwitch();
  }

  @Override
  public ThrowableSet mightThrow(Stmt u) {
    return mightThrow(u, null);
  }

  public ThrowableSet mightThrow(Stmt u, SootMethod sm) {
    UnitSwitch sw = unitSwitch(sm);
    u.apply(sw);
    return sw.getResult();
  }


  @Override
  public ThrowableSet mightThrowImplicitly(ThrowInst t) {
    return implicitThrowExceptions;
  }

  @Override
  public ThrowableSet mightThrowImplicitly(JThrowStmt t) {
    return implicitThrowExceptions;
  }

  protected ThrowableSet mightThrow(Value v) {
    ValueSwitch sw = valueSwitch();
    v.apply(sw);
    return sw.getResult();
  }

  protected ThrowableSet mightThrow(SootMethodRef m) {
    // The throw analysis is used in the front-ends. Conseqeuently, some
    // methods might not yet be loaded. If this is the case, we make
    // conservative assumptions.
    SootMethod sm = m.tryResolve();
    if (sm != null) {
      return mightThrow(sm);
    } else {
      return mgr.ALL_THROWABLES;
    }
  }

  /**
   * Returns the set of types that might be thrown as a result of calling the specified method.
   *
   * @param sm
   *          method whose exceptions are to be returned.
   *
   * @return a representation of the set of {@link Throwable Throwable} types that <code>m</code> might throw.
   */
  protected ThrowableSet mightThrow(SootMethod sm) {
    if (!isInterproc) {
      return ThrowableSet.Manager.v().ALL_THROWABLES;
    }
    return methodToThrowSet.getUnchecked(sm);
  }

  protected final LoadingCache<SootMethod, ThrowableSet> methodToThrowSet
      = IDESolver.DEFAULT_CACHE_BUILDER.build(new CacheLoader<SootMethod, ThrowableSet>() {
        @Override
        public ThrowableSet load(SootMethod sm) throws Exception {
          return mightThrow(sm, new HashSet<SootMethod>());
        }
      });

  /**
   * Returns the set of types that might be thrown as a result of calling the specified method.
   *
   * @param sm
   *          method whose exceptions are to be returned.
   * @param doneSet
   *          The set of methods that were already processed
   *
   * @return a representation of the set of {@link Throwable Throwable} types that <code>m</code> might throw.
   */
  private ThrowableSet mightThrow(SootMethod sm, Set<SootMethod> doneSet) {
    // Do not run in loops
    if (!doneSet.add(sm)) {
      return ThrowableSet.Manager.v().EMPTY;
    }

    // If we don't have body, we silently ignore the method. This is
    // unsound, but would otherwise always bloat our result set.
    if (!sm.hasActiveBody()) {
      return ThrowableSet.Manager.v().EMPTY;
    }

    // We need a mapping between unit and exception
    final PatchingChain<Stmt> stmts = sm.getActiveBody().getUnits();
    Map<Stmt, Collection<Trap>> unitToTraps
        = sm.getActiveBody().getTraps().isEmpty() ? null : new HashMap<Stmt, Collection<Trap>>();
    for (Trap t : sm.getActiveBody().getTraps()) {
      for (Iterator<Stmt> unitIt = stmts.iterator(t.getBeginUnit(), stmts.getPredOf(t.getEndUnit())); unitIt.hasNext();) {
        Stmt unit = unitIt.next();

        Collection<Trap> unitsForTrap = unitToTraps.get(unit);
        if (unitsForTrap == null) {
          unitsForTrap = new ArrayList<Trap>();
          unitToTraps.put(unit, unitsForTrap);
        }
        unitsForTrap.add(t);
      }
    }

    ThrowableSet methodSet = ThrowableSet.Manager.v().EMPTY;
    if (sm.hasActiveBody()) {
      Body methodBody = sm.getActiveBody();

      for (Stmt s : methodBody.getUnits()) {
        if (s instanceof Stmt) {
          Stmt stmt = (Stmt) s;

          ThrowableSet curStmtSet;
          if (stmt.containsInvokeExpr()) {
            AbstractInvokeExpr inv = stmt.getInvokeExpr();
            curStmtSet = mightThrow(inv.getMethod(), doneSet);
          } else {
            curStmtSet = mightThrow(s, sm);
          }

          // The exception might be caught along the way
          if (unitToTraps != null) {
            Collection<Trap> trapsForUnit = unitToTraps.get(stmt);
            if (trapsForUnit != null) {
              for (Trap t : trapsForUnit) {
                ThrowableSet.Pair p = curStmtSet.whichCatchableAs(t.getException().getType());
                curStmtSet = curStmtSet.remove(p.getCaught());
              }
            }
          }

          methodSet = methodSet.add(curStmtSet);
        }
      }
    }

    return methodSet;
  }

  private static final IntConstant INT_CONSTANT_ZERO = IntConstant.getInstance(0);
  private static final LongConstant LONG_CONSTANT_ZERO = LongConstant.getInstance(0);

  protected class UnitSwitch extends AbstractStmtVisitor<ThrowableSet> {

    // Asynchronous errors are always possible:
    protected ThrowableSet result = defaultResult();
    protected SootMethod sm;

    public UnitSwitch(SootMethod sm) {
      this.sm = sm;
    }

    public ThrowableSet getResult() {
      return result;
    }


    @Override
    public void caseAssignStmt(JAssignStmt s) {
      Value lhs = s.getLeftOp();
      if (lhs instanceof JArrayRef && (lhs.getType() instanceof UnknownType || lhs.getType() instanceof ReferenceType)) {
        // This corresponds to an aastore byte code.
        result = result.add(mgr.ARRAY_STORE_EXCEPTION);
      }
      result = result.add(mightThrow(s.getLeftOp()));
      result = result.add(mightThrow(s.getRightOp()));
    }

    @Override
    public void caseBreakpointStmt(JBreakpointStmt s) {
    }

    @Override
    public void caseEnterMonitorStmt(JEnterMonitorStmt s) {
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(s.getOp()));
    }

    @Override
    public void caseExitMonitorStmt(JExitMonitorStmt s) {
      result = result.add(mgr.ILLEGAL_MONITOR_STATE_EXCEPTION);
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(s.getOp()));
    }

    @Override
    public void caseGotoStmt(JGotoStmt s) {
    }

    @Override
    public void caseIdentityStmt(JIdentityStmt s) {
    }
    // Perhaps IdentityStmt shouldn't even return VM_ERRORS,
    // since it corresponds to no bytecode instructions whatsoever.

    @Override
    public void caseIfStmt(JIfStmt s) {
      result = result.add(mightThrow(s.getCondition()));
    }

    @Override
    public void caseInvokeStmt(JInvokeStmt s) {
      result = result.add(mightThrow(s.getInvokeExpr()));
    }

    @Override
    public void caseSwitchStmt(JSwitchStmt s) {
      result = result.add(mightThrow(s.getKey()));
    }

    @Override
    public void caseNopStmt(JNopStmt s) {
    }

    @Override
    public void caseRetStmt(JRetStmt s) {
      // Soot should never produce any RetStmt, since
      // it implements jsr with gotos.
    }

    @Override
    public void caseReturnStmt(JReturnStmt s) {
      // result = result.add(mgr.ILLEGAL_MONITOR_STATE_EXCEPTION);
      // result = result.add(mightThrow(s.getOp()));
    }

    @Override
    public void caseReturnVoidStmt(JReturnVoidStmt s) {
      // result = result.add(mgr.ILLEGAL_MONITOR_STATE_EXCEPTION);
    }


    @Override
    public void caseThrowStmt(JThrowStmt s) {
      result = mightThrowImplicitly(s);
      result = result.add(mightThrowExplicitly(s, sm));
    }

    @Override
    public void defaultCase(Object obj) {
    }
  }

  protected class ValueSwitch implements GrimpValueSwitch, ShimpleValueSwitch {

    // Asynchronous errors are always possible:
    protected ThrowableSet result = defaultResult();

    ThrowableSet getResult() {
      return result;
    }

    // Declared by ConstantSwitch interface:

    @Override
    public void caseDoubleConstant(DoubleConstant c) {
    }

    @Override
    public void caseFloatConstant(FloatConstant c) {
    }

    @Override
    public void caseIntConstant(IntConstant c) {
    }

    @Override
    public void caseLongConstant(LongConstant c) {
    }

    @Override
    public void caseNullConstant(NullConstant c) {
    }

    @Override
    public void caseStringConstant(StringConstant c) {
    }

    @Override
    public void caseClassConstant(ClassConstant c) {
    }

    @Override
    public void caseMethodHandle(MethodHandle handle) {
    }

    @Override
    public void caseMethodType(MethodType type) {
    }

    // Declared by ExprSwitch interface:

    @Override
    public void caseAddExpr(AddExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseAndExpr(AndExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmpExpr(CmpExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmpgExpr(CmpgExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmplExpr(CmplExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseDivExpr(DivExpr expr) {
      caseBinopDivExpr(expr);
    }

    @Override
    public void caseEqExpr(EqExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseNeExpr(NeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseGeExpr(GeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseGtExpr(GtExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseLeExpr(LeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseLtExpr(LtExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseMulExpr(MulExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseOrExpr(OrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseRemExpr(RemExpr expr) {
      caseBinopDivExpr(expr);
    }

    @Override
    public void caseShlExpr(ShlExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseShrExpr(ShrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseUshrExpr(UshrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseSubExpr(SubExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseXorExpr(XorExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseInterfaceInvokeExpr(InterfaceInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseSpecialInvokeExpr(SpecialInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseStaticInvokeExpr(StaticInvokeExpr expr) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
      for (int i = 0; i < expr.getArgCount(); i++) {
        result = result.add(mightThrow(expr.getArg(i)));
      }
      result = result.add(mightThrow(expr.getMethodRef()));
    }

    @Override
    public void caseVirtualInvokeExpr(VirtualInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    // INSERTED for invokedynamic StmtThrowAnalysis.java
    @Override
    public void caseDynamicInvokeExpr(DynamicInvokeExpr expr) {
      // caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseCastExpr(CastExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      Type fromType = expr.getOp().getType();
      Type toType = expr.getCastType();
      if (toType instanceof RefLikeType) {
        // fromType might still be unknown when we are called,
        // but toType will have a value.
        FastHierarchy h = Scene.v().getOrMakeFastHierarchy();
        if (fromType == null || fromType instanceof UnknownType
            || ((!(fromType instanceof NullType)) && (!h.canStoreType(fromType, toType)))) {
          result = result.add(mgr.CLASS_CAST_EXCEPTION);
        }
      }
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseInstanceOfExpr(InstanceOfExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseNewArrayExpr(NewArrayExpr expr) {
      if (expr.getBaseType() instanceof RefLikeType) {
        result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      }
      Value count = expr.getSize();
      if ((!(count instanceof IntConstant)) || (((IntConstant) count).isLessThan(INT_CONSTANT_ZERO))) {
        result = result.add(mgr.NEGATIVE_ARRAY_SIZE_EXCEPTION);
      }
      result = result.add(mightThrow(count));
    }

    @Override
    public void caseNewMultiArrayExpr(NewMultiArrayExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      for (int i = 0; i < expr.getSizeCount(); i++) {
        Value count = expr.getSize(i);
        if ((!(count instanceof IntConstant)) || (((IntConstant) count).isLessThan(INT_CONSTANT_ZERO))) {
          result = result.add(mgr.NEGATIVE_ARRAY_SIZE_EXCEPTION);
        }
        result = result.add(mightThrow(count));
      }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void caseNewExpr(NewExpr expr) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
      for (ValueBox box : expr.getUseBoxes()) {
        result = result.add(mightThrow(box.getValue()));
      }
    }

    @Override
    public void caseLengthExpr(LengthExpr expr) {
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseNegExpr(NegExpr expr) {
      result = result.add(mightThrow(expr.getOp()));
    }

    // Declared by RefSwitch interface:

    @Override
    public void caseArrayRef(ArrayRef ref) {
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mgr.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
      result = result.add(mightThrow(ref.getBase()));
      result = result.add(mightThrow(ref.getIndex()));
    }

    @Override
    public void caseStaticFieldRef(StaticFieldRef ref) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
    }

    @Override
    public void caseInstanceFieldRef(InstanceFieldRef ref) {
      result = result.add(mgr.RESOLVE_FIELD_ERRORS);
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(ref.getBase()));
    }

    @Override
    public void caseParameterRef(ParameterRef v) {
    }

    @Override
    public void caseCaughtExceptionRef(CaughtExceptionRef v) {
    }

    @Override
    public void caseThisRef(ThisRef v) {
    }

    @Override
    public void caseLocal(Local l) {
    }

    @Override
    public void caseNewInvokeExpr(NewInvokeExpr e) {
      caseStaticInvokeExpr(e);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void casePhiExpr(PhiExpr e) {
      for (ValueBox box : e.getUseBoxes()) {
        result = result.add(mightThrow(box.getValue()));
      }
    }

    @Override
    public void defaultCase(Object obj) {
    }

    // The remaining cases are not declared by GrimpValueSwitch,
    // but are used to factor out code common to several cases.

    private void caseBinopExpr(BinopExpr expr) {
      result = result.add(mightThrow(expr.getOp1()));
      result = result.add(mightThrow(expr.getOp2()));
    }

    private void caseBinopDivExpr(BinopExpr expr) {
      // Factors out code common to caseDivExpr and caseRemExpr.
      // The checks against constant divisors would perhaps be
      // better performed in a later pass, post-constant-propagation.
      Value divisor = expr.getOp2();
      Type divisorType = divisor.getType();
      if (divisorType instanceof UnknownType) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      } else if ((divisorType instanceof IntegerType)
          && ((!(divisor instanceof IntConstant)) || (((IntConstant) divisor).equals(INT_CONSTANT_ZERO)))) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      } else if ((divisorType == LongType.v())
          && ((!(divisor instanceof LongConstant)) || (((LongConstant) divisor).equals(LONG_CONSTANT_ZERO)))) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      }
      caseBinopExpr(expr);
    }

    private void caseInstanceInvokeExpr(InstanceInvokeExpr expr) {
      result = result.add(mgr.RESOLVE_METHOD_ERRORS);
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      for (int i = 0; i < expr.getArgCount(); i++) {
        result = result.add(mightThrow(expr.getArg(i)));
      }
      result = result.add(mightThrow(expr.getBase()));
      result = result.add(mightThrow(expr.getMethodRef()));
    }
  }
}
