package de.upb.swt.soot.core.jimple.visitor;

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

import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.expr.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Replace old use of an expr with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseExprVisitor extends AbstractExprVisitor<Expr> {

  private Immediate oldUse;
  private Immediate newUse;
  Block phiBlock = null;

  public ReplaceUseExprVisitor() {}

  public void init(@Nonnull Immediate oldUse, @Nonnull Immediate newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  /* This constructor is for PhiExpr. The phiBlock is a block which newUse belongs to.*/
  public ReplaceUseExprVisitor(Immediate oldUse, Immediate newUse, Block phiBlock) {
    this.oldUse = oldUse;
    this.newUse = newUse;
    this.phiBlock = phiBlock;
  }

  @Override
  public void caseAddExpr(@Nonnull JAddExpr expr) {
    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newAddExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseAndExpr(@Nonnull JAndExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newAndExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmpExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmpgExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newCmplExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseDivExpr(@Nonnull JDivExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newDivExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseEqExpr(@Nonnull JEqExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newEqExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNeExpr(@Nonnull JNeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newNeExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseGeExpr(@Nonnull JGeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newGeExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseGtExpr(@Nonnull JGtExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newGtExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLeExpr(@Nonnull JLeExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newLeExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLtExpr(@Nonnull JLtExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newLtExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseMulExpr(@Nonnull JMulExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newMulExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseOrExpr(@Nonnull JOrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newOrExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseRemExpr(@Nonnull JRemExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newRemExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseShlExpr(@Nonnull JShlExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newShlExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseShrExpr(@Nonnull JShrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newShrExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newUshrExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseSubExpr(@Nonnull JSubExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newSubExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseXorExpr(@Nonnull JXorExpr expr) {

    if (expr.getOp1() == oldUse && expr.getOp2() == oldUse) {
      setResult(Jimple.newXorExpr(newUse, newUse));
    } else if (expr.getOp1() == oldUse) {
      setResult(expr.withOp1(newUse));
    } else if (expr.getOp2() == oldUse) {
      setResult(expr.withOp2(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Value arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, newUse);
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
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Value arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, newUse);
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
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {

    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getSizes());
    int index = 0;
    for (Immediate arg : expr.getSizes()) {
      if (arg == oldUse) {
        newArgs.set(index, newUse);
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
  public void caseSpecialInvokeExpr(@Nonnull JSpecialInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr expr) {
    instanceInvokeExpr(expr);
  }

  private void instanceInvokeExpr(@Nonnull AbstractInstanceInvokeExpr expr) {
    boolean isChanged = false;
    List<Immediate> newArgs = new ArrayList<>(expr.getArgs());
    int index = 0;
    for (Immediate arg : expr.getArgs()) {
      if (arg == oldUse) {
        newArgs.set(index, newUse);
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
  public void caseCastExpr(@Nonnull JCastExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
    if (expr.getSize() == oldUse) {
      setResult(expr.withSize(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    if (expr.getOp() == oldUse) {
      setResult(expr.withOp(newUse));
    } else {
      errorHandler(expr);
    }
  }

  @Override
  public void casePhiExpr(JPhiExpr v) {
    if (this.phiBlock != null
        && newUse instanceof Local
        && v.getArgs().contains(oldUse)
        && newUse.getType().equals(v.getType())
        && !v.getArgs().contains(newUse)) {
      List<Local> argsList = new ArrayList<>(v.getArgs());
      int index = argsList.indexOf(oldUse);
      argsList.set(index, (Local) newUse);
      v = v.withArgs(argsList);

      Map<Local, Block> newArgToBlock = new HashMap<>();
      List<Block> blocks = v.getBlocks();
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
  public void caseNewExpr(@Nonnull JNewExpr expr) {
    defaultCaseExpr(expr);
  }

  @Override
  public void defaultCaseExpr(@Nonnull Expr expr) {
    setResult(expr);
  }

  public void errorHandler(@Nonnull Expr expr) {
    defaultCaseExpr(expr);
  }
}
