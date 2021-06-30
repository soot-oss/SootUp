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

  Value oldUse;
  Value newUse;
  Block phiBlock = null;

  public ReplaceUseExprVisitor(Value oldUse, Value newUse) {
    this.oldUse = oldUse;
    this.newUse = newUse;
  }

  /* This constructor is for PhiExpr. The phiBlock is a block which newUse belongs to.*/
  public ReplaceUseExprVisitor(Value oldUse, Value newUse, Block phiBlock) {
    this.oldUse = oldUse;
    this.newUse = newUse;
    this.phiBlock = phiBlock;
  }

  @Override
  public void caseAddExpr(@Nonnull JAddExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseAndExpr(@Nonnull JAndExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseDivExpr(@Nonnull JDivExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseEqExpr(@Nonnull JEqExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseNeExpr(@Nonnull JNeExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseGeExpr(@Nonnull JGeExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseGtExpr(@Nonnull JGtExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseLeExpr(@Nonnull JLeExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseLtExpr(@Nonnull JLtExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseMulExpr(@Nonnull JMulExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseOrExpr(@Nonnull JOrExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseRemExpr(@Nonnull JRemExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseShlExpr(@Nonnull JShlExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseShrExpr(@Nonnull JShrExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseSubExpr(@Nonnull JSubExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseXorExpr(@Nonnull JXorExpr expr) {
    if (newUse instanceof Immediate) {
      if (expr.getOp1().equivTo(oldUse) && expr.getOp2().equivTo(oldUse)) {
        setResult(Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse));
      } else if (expr.getOp1().equivTo(oldUse)) {
        setResult(expr.withOp1(newUse));
      } else if (expr.getOp2().equivTo(oldUse)) {
        setResult(expr.withOp2(newUse));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr expr) {
    if (newUse instanceof Immediate) {
      boolean isChanged = false;
      List<Value> newArgs = new ArrayList<>(expr.getArgs());
      int index = 0;
      for (Value arg : expr.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
      if (isChanged) {
        setResult(expr.withArgs(newArgs));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr expr) {
    if (newUse instanceof Immediate) {
      boolean isChanged = false;
      List<Value> newArgs = new ArrayList<>(expr.getArgs());
      int index = 0;
      for (Value arg : expr.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
      if (isChanged) {
        setResult(expr.withMethodArgs(newArgs));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr expr) {
    if (newUse instanceof Immediate) {
      boolean isChanged = false;
      List<Value> newArgs = new ArrayList<>(expr.getSizes());
      int index = 0;
      for (Value arg : expr.getSizes()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
      if (isChanged) {
        setResult(expr.withSizes(newArgs));
      } else {
        defaultCaseExpr(expr);
      }
    } else {
      defaultCaseExpr(expr);
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
    if (newUse instanceof Immediate) {
      List<Value> newArgs = new ArrayList<>(expr.getArgs());
      int index = 0;
      for (Value arg : expr.getArgs()) {
        if (arg.equivTo(oldUse)) {
          newArgs.set(index, newUse);
          isChanged = true;
        }
        index++;
      }
      if (isChanged) {
        setResult(expr.withArgs(newArgs));
      }
    }
    if (newUse instanceof Local && expr.getBase().equivTo(oldUse)) {
      if (isChanged) {
        setResult(((JSpecialInvokeExpr) getResult()).withBase((Local) newUse));
      } else {
        setResult(expr.withBase((Local) newUse));
        isChanged = true;
      }
    }
    if (!isChanged) {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseCastExpr(@Nonnull JCastExpr expr) {
    if (newUse instanceof Immediate && expr.getOp().equivTo(oldUse)) {
      setResult(expr.withOp(newUse));
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr expr) {
    if (newUse instanceof Immediate && expr.getOp().equivTo(oldUse)) {
      setResult(expr.withOp(newUse));
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr expr) {
    if (newUse instanceof Immediate && expr.getSize().equivTo(oldUse)) {
      setResult(expr.withSize(newUse));
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr expr) {
    if (newUse instanceof Immediate && expr.getOp().equivTo(oldUse)) {
      setResult(expr.withOp(newUse));
    } else {
      defaultCaseExpr(expr);
    }
  }

  @Override
  public void caseNegExpr(@Nonnull JNegExpr expr) {
    if (newUse instanceof Immediate && expr.getOp().equivTo(oldUse)) {
      setResult(expr.withOp(newUse));
    } else {
      defaultCaseExpr(expr);
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
}
