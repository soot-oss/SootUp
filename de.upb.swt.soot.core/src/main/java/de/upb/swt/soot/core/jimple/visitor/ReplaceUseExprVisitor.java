package de.upb.swt.soot.core.jimple.visitor;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.*;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Replace old use of an expr with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseExprVisitor extends AbstractExprVisitor {

  Value oldUse;
  Immediate newUse;
  Expr newExpr;

  public ReplaceUseExprVisitor(Value oldUse, Immediate newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  @Nonnull
  @Override
  public void caseAddExpr(@Nonnull JAddExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newAddExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newAddExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newAddExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseAndExpr(@Nonnull JAndExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newAndExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newAndExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newAndExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmpExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newCmpExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmpExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmpgExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newCmpgExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmpgExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmplExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newCmplExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newCmplExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseDivExpr(@Nonnull JDivExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newDivExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newDivExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newDivExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseEqExpr(@Nonnull JEqExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newEqExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newEqExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newEqExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNeExpr(@Nonnull JNeExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newNeExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newNeExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newNeExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseGeExpr(@Nonnull JGeExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newGeExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newGeExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newGeExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseGtExpr(@Nonnull JGtExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newGtExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newGtExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newGtExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseLeExpr(@Nonnull JLeExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newLeExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newLeExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newLeExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseLtExpr(@Nonnull JLtExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newLtExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newLtExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newLtExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseMulExpr(@Nonnull JMulExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newMulExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newMulExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newMulExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseOrExpr(@Nonnull JOrExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newOrExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newOrExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newOrExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseRemExpr(@Nonnull JRemExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newRemExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newRemExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newRemExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseShlExpr(@Nonnull JShlExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newShlExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newShlExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newShlExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseShrExpr(@Nonnull JShrExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newShrExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newShrExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newShrExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newUshrExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newUshrExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newUshrExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseSubExpr(@Nonnull JSubExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newSubExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newSubExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newSubExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseXorExpr(@Nonnull JXorExpr v) {
    if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newXorExpr(newUse, newUse);
    } else if (v.getOp1().equivTo(oldUse)) {
      newExpr = Jimple.newXorExpr(newUse, (Immediate) v.getOp2());
    } else if (v.getOp2().equivTo(oldUse)) {
      newExpr = Jimple.newXorExpr((Immediate) v.getOp1(), newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  // args[] is Immediate[]
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr v) {
    boolean isChanged = false;
    List<Value> newArgs = new ArrayList<Value>(v.getArgs());
    int index = 0;
    if (!v.getArgs().isEmpty()) {
      for (Value arg : v.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged) {
      newExpr = v.withArgs(newArgs);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  // base is Local
  // args[] is Immediate[]
  public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr v) {
    boolean isChanged = false;
    List<Value> newArgs = new ArrayList<Value>(v.getArgs());
    int index = 0;
    if (!v.getArgs().isEmpty()) {
      for (Value arg : v.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged && v.getBase().equivTo(oldUse)) {
      v = v.withBase((Local) newUse);
      newExpr = v.withArgs(newArgs);
    } else if ((!isChanged) && v.getBase().equivTo(oldUse)) {
      newExpr = v.withBase((Local) newUse);
    } else if (isChanged && (!v.getBase().equivTo(oldUse))) {
      newExpr = v.withArgs(newArgs);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr v) {
    boolean isChanged = false;
    List<Value> newArgs = new ArrayList<Value>(v.getArgs());
    int index = 0;
    if (!v.getArgs().isEmpty()) {
      for (Value arg : v.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged && v.getBase().equivTo(oldUse)) {
      v = v.withBase(newUse);
      newExpr = v.withArgs(newArgs);
    } else if ((!isChanged) && v.getBase().equivTo(oldUse)) {
      newExpr = v.withBase(newUse);
    } else if (isChanged && (!v.getBase().equivTo(oldUse))) {
      newExpr = v.withArgs(newArgs);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr v) {
    boolean isChanged = false;
    List<Value> newArgs = new ArrayList<Value>(v.getArgs());
    int index = 0;
    if (!v.getArgs().isEmpty()) {
      for (Value arg : v.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged && v.getBase().equivTo(oldUse)) {
      v = v.withBase(newUse);
      newExpr = v.withArgs(newArgs);
    } else if ((!isChanged) && v.getBase().equivTo(oldUse)) {
      newExpr = v.withBase(newUse);
    } else if (isChanged && (!v.getBase().equivTo(oldUse))) {
      newExpr = v.withArgs(newArgs);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr v) {
    boolean isChanged = false;
    List<Value> newArgs = new ArrayList<Value>(v.getArgs());
    int index = 0;
    if (!v.getArgs().isEmpty()) {
      for (Value arg : v.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged) {
      newExpr = v.withMethodArgs(newArgs);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCastExpr(@Nonnull JCastExpr v) {
    if (v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr v) {
    if (v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr v) {
    if (v.getSize().equivTo(oldUse)) {
      newExpr = v.withSize(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr v) {
    boolean isChanged = false;
    List<Value> newSizes = new ArrayList<Value>(v.getSizes());
    int index = 0;
    if (!v.getSizes().isEmpty()) {
      for (Value arg : v.getSizes()) {
        if (arg.equivTo(oldUse)) {
          newSizes.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
    }
    if (isChanged) {
      newExpr = v.withSizes(newSizes);
    } else {
      defaultCase(v);
    }
  }

  @Override
  public void caseNewExpr(JNewExpr v) {
    defaultCase(v);
  }

  @Nonnull
  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr v) {
    if (v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNegExpr(@Nonnull JNegExpr v) {
    if (v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  public Expr getNewExpr() {
    return newExpr;
  }

  @Nonnull
  @Override
  public void defaultCase(@Nonnull Object v) {
    newExpr = (Expr) v;
  }
}
