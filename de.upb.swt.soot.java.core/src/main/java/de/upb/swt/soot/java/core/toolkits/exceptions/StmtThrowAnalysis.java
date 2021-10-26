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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.constant.*;
import de.upb.swt.soot.core.jimple.common.expr.*;
import de.upb.swt.soot.core.jimple.common.ref.*;
import de.upb.swt.soot.core.jimple.common.stmt.*;
import de.upb.swt.soot.core.jimple.javabytecode.stmt.*;
import de.upb.swt.soot.core.jimple.visitor.AbstractStmtVisitor;
import de.upb.swt.soot.core.jimple.visitor.AbstractValueVisitor;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
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

  protected final ThrowableSet.Manager mgr = ThrowableSet.Manager.getInstance();

  // Cache the response to mightThrowImplicitly():
  private final ThrowableSet implicitThrowExceptions = ThrowableSet.Manager.getInstance().VM_ERRORS
      .add(ThrowableSet.Manager.getInstance().NULL_POINTER_EXCEPTION).add(ThrowableSet.Manager.getInstance().ILLEGAL_MONITOR_STATE_EXCEPTION);

  /**
   * Constructs a <code>UnitThrowAnalysis</code> for inclusion in Soot's global variable manager.
   */
  public StmtThrowAnalysis(View view) {
    this(view,false);
  }

  protected final boolean isInterproc;

  public StmtThrowAnalysis(View view, boolean isInterproc) {
    super(view);
    this.isInterproc = isInterproc;
  }

  /**
   * Returns the single instance of <code>UnitThrowAnalysis</code>.
   *
   * @return Soot's <code>UnitThrowAnalysis</code>.
   */

  protected ThrowableSet defaultResult() {
    return mgr.VM_ERRORS;
  }

  protected StmtSwitch unitSwitch(SootMethod sm) {
    return new StmtSwitch(sm);
  }

  protected ValueSwitch valueSwitch() {
    return new ValueSwitch();
  }

  @Override
  public ThrowableSet mightThrow(Stmt u) {
    return mightThrow(u, null);
  }

  public ThrowableSet mightThrow(Stmt u, SootMethod sm) {
    StmtSwitch sw = unitSwitch(sm);
    u.accept(sw);
    return sw.getResult();
  }



  @Override
  public ThrowableSet mightThrowImplicitly(JThrowStmt t) {
    return implicitThrowExceptions;
  }

  protected ThrowableSet mightThrow(Value v) {
    ValueSwitch sw = valueSwitch();
    v.accept(sw);
    return sw.getResult();
  }

  protected ThrowableSet mightThrow(MethodSignature m) {
    // The throw analysis is used in the front-ends. Conseqeuently, some
    // methods might not yet be loaded. If this is the case, we make
    // conservative assumptions.
    Optional<? extends SootMethod> sm = view.getMethod(m);
    if (sm.isPresent()) {
      return mightThrow(sm.get());
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
      return ThrowableSet.Manager.getInstance().ALL_THROWABLES;
    }
    return methodToThrowSet.getUnchecked(sm);
  }
  private static CacheBuilder<Object, Object> DEFAULT_CACHE_BUILDER = CacheBuilder.newBuilder().concurrencyLevel(Runtime.getRuntime().availableProcessors()).initialCapacity(10000).softValues();

  protected final LoadingCache<SootMethod, ThrowableSet> methodToThrowSet
      = DEFAULT_CACHE_BUILDER.build(new CacheLoader<SootMethod, ThrowableSet>() {
        @Override
        public ThrowableSet load(SootMethod sm) throws Exception {
          return mightThrow(sm.getSignature(), new HashSet<MethodSignature>());
        }
      });

  /**
   * Returns the set of types that might be thrown as a result of calling the specified method.
   *
   * @param methodSignature
   *          method whose exceptions are to be returned.
   * @param doneSet
   *          The set of methods that were already processed
   *
   * @return a representation of the set of {@link Throwable Throwable} types that <code>m</code> might throw.
   */
  private ThrowableSet mightThrow(MethodSignature methodSignature, Set<MethodSignature> doneSet) {
    // Do not run in loops
    SootMethod sm = view.getMethod(methodSignature).get();
    if (!doneSet.add(sm.getSignature())) {
      return ThrowableSet.Manager.getInstance().EMPTY;
    }

    // If we don't have body, we silently ignore the method. This is
    // unsound, but would otherwise always bloat our result set.
    if (!sm.hasBody()) {
      return ThrowableSet.Manager.getInstance().EMPTY;
    }

    // We need a mapping between unit and exception
    final StmtGraph stmts = sm.getBody().getStmtGraph();
    Map<Stmt, Collection<Trap>> unitToTraps
        = sm.getBody().getTraps().isEmpty() ? null : new HashMap<Stmt, Collection<Trap>>();
    for (Trap t : sm.getBody().getTraps()) {
      // FIXME - how to adapt branching factor? stmts.iterator is not the way it works (Legacy Code incidentally occured)
      for (Iterator<Stmt> unitIt = stmts.iterator(t.getBeginStmt(), stmts.getPredOf(t.getEndStmt())); unitIt.hasNext();) {
        Stmt unit = unitIt.next();
        Collection<Trap> unitsForTrap = unitToTraps.computeIfAbsent(unit, k -> new ArrayList<>());
        unitsForTrap.add(t);
      }
    }

    ThrowableSet methodSet = ThrowableSet.Manager.getInstance().EMPTY;
    if (sm.hasBody()) {
      Body methodBody = sm.getBody();

      for (Stmt stmt : methodBody.getStmts()) {
          ThrowableSet curStmtSet;
          if (stmt.containsInvokeExpr()) {
            AbstractInvokeExpr inv = stmt.getInvokeExpr();
            curStmtSet = mightThrow(inv.getMethodSignature(), doneSet);
          } else {
            curStmtSet = mightThrow(stmt, sm);
          }

          // The exception might be caught along the way
          if (unitToTraps != null) {
            Collection<Trap> trapsForUnit = unitToTraps.get(stmt);
            if (trapsForUnit != null) {
              for (Trap t : trapsForUnit) {
                ThrowableSet.Pair p = curStmtSet.whichCatchableAs(t.getExceptionType());
                curStmtSet = curStmtSet.remove(p.getCaught());
              }
            }
          }

          methodSet = methodSet.add(curStmtSet);
        }
      }

    return methodSet;
  }

  private static final IntConstant INT_CONSTANT_ZERO = IntConstant.getInstance(0);
  private static final LongConstant LONG_CONSTANT_ZERO = LongConstant.getInstance(0);

  protected class StmtSwitch extends AbstractStmtVisitor<ThrowableSet> {

    // Asynchronous errors are always possible:
    protected ThrowableSet result = defaultResult();
    protected SootMethod sm;

    public StmtSwitch(SootMethod sm) {
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

  }

  protected class ValueSwitch extends AbstractValueVisitor<ThrowableSet> {

    // Asynchronous errors are always possible:
    protected ThrowableSet result = defaultResult();

    public ThrowableSet getResult() {
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
    public void caseAddExpr(JAddExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseAndExpr(JAndExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmpExpr(JCmpExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmpgExpr(JCmpgExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseCmplExpr(JCmplExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseDivExpr(JDivExpr expr) {
      caseBinopDivExpr(expr);
    }

    @Override
    public void caseEqExpr(JEqExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseNeExpr(JNeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseGeExpr(JGeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseGtExpr(JGtExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseLeExpr(JLeExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseLtExpr(JLtExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseMulExpr(JMulExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseOrExpr(JOrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseRemExpr(JRemExpr expr) {
      caseBinopDivExpr(expr);
    }

    @Override
    public void caseShlExpr(JShlExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseShrExpr(JShrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseUshrExpr(JUshrExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseSubExpr(JSubExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseXorExpr(JXorExpr expr) {
      caseBinopExpr(expr);
    }

    @Override
    public void caseInterfaceInvokeExpr(JInterfaceInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseSpecialInvokeExpr(JSpecialInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseStaticInvokeExpr(JStaticInvokeExpr expr) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
      for (int i = 0; i < expr.getArgCount(); i++) {
        result = result.add(mightThrow(expr.getArg(i)));
      }
      result = result.add(mightThrow(expr.getMethodSignature()));
    }

    @Override
    public void caseVirtualInvokeExpr(JVirtualInvokeExpr expr) {
      caseInstanceInvokeExpr(expr);
    }

    // INSERTED for invokedynamic StmtThrowAnalysis.java
    @Override
    public void caseDynamicInvokeExpr(JDynamicInvokeExpr expr) {
      // caseInstanceInvokeExpr(expr);
    }

    @Override
    public void caseCastExpr(JCastExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      Type fromType = expr.getOp().getType();
      Type toType = expr.getType();
      if (toType instanceof ReferenceType) {
        // fromType might still be unknown when we are called,
        // but toType will have a value.
        TypeHierarchy h = view. Scene.v().getOrMakeFastHierarchy();
        if (fromType == null || fromType instanceof UnknownType
            || ((!(fromType instanceof NullType)) && (!h.canStoreType(fromType, toType)))) {
          result = result.add(mgr.CLASS_CAST_EXCEPTION);
        }
      }
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseInstanceOfExpr(JInstanceOfExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseNewArrayExpr(JNewArrayExpr expr) {
      if (expr.getBaseType() instanceof ReferenceType) {
        result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      }
      Value count = expr.getSize();
      if ((!(count instanceof IntConstant)) || (((IntConstant) count).lessThan(INT_CONSTANT_ZERO)) == BooleanConstant.getTrue()) {
        result = result.add(mgr.NEGATIVE_ARRAY_SIZE_EXCEPTION);
      }
      result = result.add(mightThrow(count));
    }

    @Override
    public void caseNewMultiArrayExpr(JNewMultiArrayExpr expr) {
      result = result.add(mgr.RESOLVE_CLASS_ERRORS);
      for (int i = 0; i < expr.getSizeCount(); i++) {
        Value count = expr.getSize(i);
        if ((!(count instanceof IntConstant)) || (((IntConstant) count).lessThan(INT_CONSTANT_ZERO)) == BooleanConstant.getTrue()) {
          result = result.add(mgr.NEGATIVE_ARRAY_SIZE_EXCEPTION);
        }
        result = result.add(mightThrow(count));
      }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public void caseNewExpr(JNewExpr expr) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
      for (Value value : expr.getUses()) {
        result = result.add(mightThrow(value));
      }
    }

    @Override
    public void caseLengthExpr(JLengthExpr expr) {
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(expr.getOp()));
    }

    @Override
    public void caseNegExpr(JNegExpr expr) {
      result = result.add(mightThrow(expr.getOp()));
    }

    // Declared by RefSwitch interface:

    @Override
    public void caseArrayRef(JArrayRef ref) {
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mgr.ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION);
      result = result.add(mightThrow(ref.getBase()));
      result = result.add(mightThrow(ref.getIndex()));
    }

    @Override
    public void caseStaticFieldRef(JStaticFieldRef ref) {
      result = result.add(mgr.INITIALIZATION_ERRORS);
    }

    @Override
    public void caseInstanceFieldRef(JInstanceFieldRef ref) {
      result = result.add(mgr.RESOLVE_FIELD_ERRORS);
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      result = result.add(mightThrow(ref.getBase()));
    }

    @Override
    public void caseParameterRef(JParameterRef v) {
    }

    @Override
    public void caseCaughtExceptionRef(JCaughtExceptionRef v) {
    }

    @Override
    public void caseThisRef(JThisRef v) {
    }

    @Override
    public void caseLocal(Local l) {
    }

    // FIXME - when SSA branch is merged
    /*
    @SuppressWarnings("rawtypes")
    @Override
    public void casePhiExpr(PhiExpr e) {
      for (Value value : e.getUses()) {
        result = result.add(mightThrow(value));
      }
    }
    */

    // The remaining cases are not declared by GrimpValueSwitch,
    // but are used to factor out code common to several cases.

    private void caseBinopExpr(AbstractBinopExpr expr) {
      result = result.add(mightThrow(expr.getOp1()));
      result = result.add(mightThrow(expr.getOp2()));
    }

    private void caseBinopDivExpr(AbstractBinopExpr expr) {
      // Factors out code common to caseDivExpr and caseRemExpr.
      // The checks against constant divisors would perhaps be
      // better performed in a later pass, post-constant-propagation.
      Value divisor = expr.getOp2();
      Type divisorType = divisor.getType();
      if (divisorType instanceof UnknownType) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      } else if ((divisorType instanceof PrimitiveType.IntType)
          && ((!(divisor instanceof IntConstant)) || (((IntConstant) divisor).equals(INT_CONSTANT_ZERO)))) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      } else if ((divisorType == PrimitiveType.LongType.getInstance())
          && ((!(divisor instanceof LongConstant)) || (((LongConstant) divisor).equals(LONG_CONSTANT_ZERO)))) {
        result = result.add(mgr.ARITHMETIC_EXCEPTION);
      }
      caseBinopExpr(expr);
    }

    private void caseInstanceInvokeExpr(AbstractInstanceInvokeExpr expr) {
      result = result.add(mgr.RESOLVE_METHOD_ERRORS);
      result = result.add(mgr.NULL_POINTER_EXCEPTION);
      for (int i = 0; i < expr.getArgCount(); i++) {
        result = result.add(mightThrow(expr.getArg(i)));
      }
      result = result.add(mightThrow(expr.getBase()));
      result = result.add(mightThrow(expr.getMethodSignature()));
    }
  }
}
