package sootup.core.jimple.visitor;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Zun Wang
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.expr.*;

/**
 * Replace old use of an expr with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseExprVisitor extends AbstractExprVisitor {

  protected Value oldUse;
  protected Value newUse;
  protected Expr result = null;

  // TODO: [ms] is this (phiBlock) really a necessary field?
  BasicBlock<?> phiBlock = null;

  public ReplaceUseExprVisitor() {}

  public void init(@NonNull Value oldUse, @NonNull Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  /* This constructor is for PhiExpr. The phiBlock is a block which newUse belongs to.*/
  public ReplaceUseExprVisitor(Value oldUse, Value newUse, BasicBlock<?> phiBlock) {
    this.oldUse = oldUse;
    this.newUse = newUse;
    this.phiBlock = phiBlock;
  }

  @Override
  public void caseAddExpr(@NonNull JAddExpr expr) {
    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseAndExpr(@NonNull JAndExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newAndExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmpExpr(@NonNull JCmpExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmpExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmpgExpr(@NonNull JCmpgExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmpgExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmplExpr(@NonNull JCmplExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmplExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseDivExpr(@NonNull JDivExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newDivExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseEqExpr(@NonNull JEqExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newEqExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNeExpr(@NonNull JNeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newNeExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseGeExpr(@NonNull JGeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newGeExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseGtExpr(@NonNull JGtExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newGtExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLeExpr(@NonNull JLeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newLeExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLtExpr(@NonNull JLtExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newLtExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseMulExpr(@NonNull JMulExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newMulExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseOrExpr(@NonNull JOrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newOrExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseRemExpr(@NonNull JRemExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newRemExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseShlExpr(@NonNull JShlExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newShlExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseShrExpr(@NonNull JShrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newShrExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseUshrExpr(@NonNull JUshrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newUshrExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseSubExpr(@NonNull JSubExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newSubExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseXorExpr(@NonNull JXorExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newXorExpr((Immediate) newUse, (Immediate) newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1((Immediate) newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseStaticInvokeExpr(@NonNull JStaticInvokeExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Value arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, (Immediate) newUse);
        isChanged = true;
      }
      index++;
    }
    if (isChanged) {
      setResult(expr.withArgs(newArgs));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseDynamicInvokeExpr(@NonNull JDynamicInvokeExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Value arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, (Immediate) newUse);
        isChanged = true;
      }
      index++;
    }
    if (isChanged) {
      setResult(expr.withMethodArgs(newArgs));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNewMultiArrayExpr(@NonNull JNewMultiArrayExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getSizes());
    int index = 0;
    for (Immediate arg : expr.getSizes()) {
      if (arg == oldUse) {
        newArgs.set(index, (Immediate) newUse);
        isChanged = true;
      }
      index++;
    }
    if (isChanged) {
      setResult(expr.withSizes(newArgs));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseSpecialInvokeExpr(@NonNull JSpecialInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@NonNull JVirtualInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@NonNull JInterfaceInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  private void instanceInvokeExpr(@NonNull AbstractInstanceInvokeExpr expr) {
    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Immediate arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, (Immediate) newUse);
        isChanged = true;
      }
      index++;
    }
    if (isChanged) {
      setResult(expr.withArgs(newArgs));
    }

    if (expr.getBase() == oldUse) {
      if (isChanged) {
        setResult(((AbstractInstanceInvokeExpr) getResult()).withBase((Local) newUse));
      } else {
        setResult(expr.withBase((Local) newUse));
        isChanged = true;
      }
    }
    if (!isChanged) {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCastExpr(@NonNull JCastExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseInstanceOfExpr(@NonNull JInstanceOfExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNewArrayExpr(@NonNull JNewArrayExpr expr) {
    if (expr.getSize() == oldUse) {
      setResult(expr.withSize((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLengthExpr(@NonNull JLengthExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNegExpr(@NonNull JNegExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp((Immediate) newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void casePhiExpr(@NonNull JPhiExpr v) {
    if (this.phiBlock != null
        && newUse instanceof Local
        && v.getArgs().contains(oldUse)
        && newUse.getType().equals(v.getType())
        && !v.getArgs().contains(newUse)) {
      List<Local> argsList = new ArrayList<>(v.getArgs());
      int index = argsList.indexOf(oldUse);
      argsList.set(index, (Local) newUse);
      v = v.withArgs(argsList);

      Map<Local, BasicBlock<?>> newArgToBlock = new HashMap<>();
      List<BasicBlock<?>> blocks = v.getBlocks();
      for (int i = 0; i < v.getArgsSize(); i++) {
        if (i == index) {
          newArgToBlock.put((Local) newUse, phiBlock);
        } else {
          newArgToBlock.put(argsList.get(i), blocks.get(i));
        }
      }
      setResult(v.withArgToBlockMap(newArgToBlock));
    } else {
      defaultCaseExpr(v);
    }
  }

  @Override
  public void caseNewExpr(@NonNull JNewExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@NonNull Expr expr) {
    setResult(expr);
  }

  public void errorHandler(@NonNull Expr expr) {
    defaultCaseExpr(expr);
  }

  public Expr getResult() {
    return result;
  }

  protected void setResult(Expr result) {
    this.result = result;
  }
}
