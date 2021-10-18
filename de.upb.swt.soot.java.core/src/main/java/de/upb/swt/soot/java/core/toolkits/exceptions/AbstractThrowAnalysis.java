package de.upb.swt.soot.java.core.toolkits.exceptions;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.AbstractInvokeExpr;
import de.upb.swt.soot.core.jimple.common.expr.JNewExpr;
import de.upb.swt.soot.core.jimple.common.stmt.AbstractDefinitionStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JThrowStmt;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract class implementing parts of the {@link ThrowAnalysis} interface which may be common to multiple concrete
 * <code>ThrowAnalysis</code> classes. <code>AbstractThrowAnalysis</code> provides straightforward implementations of
 * {@link mightThrowExplicitly(ThrowInst)} and {@link mightThrowExplicitly(ThrowStmt)}, since concrete implementations of
 * <code>ThrowAnalysis</code> seem likely to differ mainly in their treatment of implicit exceptions.
 */
public abstract class AbstractThrowAnalysis implements ThrowAnalysis {

  abstract public ThrowableSet mightThrow(Stmt stmt);



  @Override
  public ThrowableSet mightThrowExplicitly(JThrowStmt t) {
    return mightThrowExplicitly(t, null);
  }

  public ThrowableSet mightThrowExplicitly(JThrowStmt t, SootMethod sm) {
    Value thrownExpression = t.getOp();
    Type thrownType = thrownExpression.getType();
    if (thrownType == null || thrownType instanceof UnknownType) {
      // We can't identify the type of thrownExpression, so...
      return ThrowableSet.Manager.v().ALL_THROWABLES;
    } else if (thrownType instanceof NullType) {
      ThrowableSet result = ThrowableSet.Manager.v().EMPTY;
      result = result.add(ThrowableSet.Manager.v().NULL_POINTER_EXCEPTION);
      return result;
    } else if (!(thrownType instanceof ReferenceType)) {
      throw new IllegalStateException("UnitThrowAnalysis StmtSwitch: type of throw argument is not a RefType!");
    } else {
      ThrowableSet result = ThrowableSet.Manager.v().EMPTY;
      if (thrownExpression instanceof AbstractInvokeExpr) { // JNewInvokeExpr
        // In this case, we know the exact type of the
        // argument exception.
        result = result.add(thrownType);
      } else {
        ReferenceType preciseType = null;
        // If there is only one allocation site, we know the type as well
        if (thrownExpression instanceof Local && sm != null) {
          Set<ReferenceType> types = sm.getBody().getStmts().stream().filter(stmt -> stmt instanceof AbstractDefinitionStmt)
              .map(stmt -> (AbstractDefinitionStmt ) stmt).filter(d -> d.getLeftOp() == thrownExpression).map(d -> d.getRightOp())
              .filter(o -> o instanceof JNewExpr).map(o -> (JNewExpr) o).map(n -> n.getType())
              .filter(r -> r instanceof ReferenceType).map(r -> (ReferenceType) r).collect(Collectors.toSet());
          if (types.size() == 1) {
            preciseType = types.iterator().next();
          }
        }

        if (preciseType == null) {
          result = result.add(AnySubType.v((ReferenceType) thrownType));
        } else {
          result = result.add(preciseType);
        }
      }
      return result;
    }
  }

  abstract public ThrowableSet mightThrowImplicitly(ThrowInst t);

  abstract public ThrowableSet mightThrowImplicitly(JThrowStmt t);
}
