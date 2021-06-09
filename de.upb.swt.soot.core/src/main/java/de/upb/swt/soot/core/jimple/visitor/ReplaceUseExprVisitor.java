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
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import java.util.*;
import javax.annotation.Nonnull;

/**
 * Replace old use of an expr with a new use
 *
 * @author Zun Wang
 */
public class ReplaceUseExprVisitor extends AbstractExprVisitor {

  Value oldUse;
  Value newUse;
  Block phiBlock = null;
  Expr newExpr;

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

  @Nonnull
  @Override
  public void caseAddExpr(@Nonnull JAddExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseAndExpr(@Nonnull JAndExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmpExpr(@Nonnull JCmpExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmpgExpr(@Nonnull JCmpgExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCmplExpr(@Nonnull JCmplExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseDivExpr(@Nonnull JDivExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseEqExpr(@Nonnull JEqExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNeExpr(@Nonnull JNeExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseGeExpr(@Nonnull JGeExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1((Immediate) newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2((Immediate) newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseGtExpr(@Nonnull JGtExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseLeExpr(@Nonnull JLeExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseLtExpr(@Nonnull JLtExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseMulExpr(@Nonnull JMulExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseOrExpr(@Nonnull JOrExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseRemExpr(@Nonnull JRemExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseShlExpr(@Nonnull JShlExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseShrExpr(@Nonnull JShrExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseUshrExpr(@Nonnull JUshrExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseSubExpr(@Nonnull JSubExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseXorExpr(@Nonnull JXorExpr v) {
    if (newUse instanceof Immediate) {
      if (v.getOp1().equivTo(oldUse) && v.getOp2().equivTo(oldUse)) {
        newExpr = Jimple.newAddExpr((Immediate) newUse, (Immediate) newUse);
      } else if (v.getOp1().equivTo(oldUse)) {
        newExpr = v.withOp1(newUse);
      } else if (v.getOp2().equivTo(oldUse)) {
        newExpr = v.withOp2(newUse);
      } else {
        defaultCase(v);
      }
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  // args[] is Immediate[]
  public void caseStaticInvokeExpr(@Nonnull JStaticInvokeExpr v) {
    if (newUse instanceof Immediate) {
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
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseDynamicInvokeExpr(@Nonnull JDynamicInvokeExpr v) {
    if (newUse instanceof Immediate) {
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
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNewMultiArrayExpr(@Nonnull JNewMultiArrayExpr v) {
    if (newUse instanceof Immediate) {
      boolean isChanged = false;
      List<Value> newArgs = new ArrayList<Value>(v.getSizes());
      int index = 0;
      if (!v.getSizes().isEmpty()) {
        for (Value arg : v.getSizes()) {
          if (arg.equivTo(oldUse)) {
            newArgs.set(index, newUse);
            isChanged = true;
          }
          index++;
        }
      }
      if (isChanged) {
        newExpr = v.withSizes(newArgs);
      } else {
        defaultCase(v);
      }
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
    if (newUse instanceof Immediate) {
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
      }
    }
    if (newUse instanceof Local && v.getBase().equivTo(oldUse)) {
      if (isChanged) {
        newExpr = ((JSpecialInvokeExpr) newExpr).withBase((Local) newUse);
      } else {
        newExpr = v.withBase((Local) newUse);
        isChanged = true;
      }
    }
    if (!isChanged) {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseVirtualInvokeExpr(@Nonnull JVirtualInvokeExpr v) {
    boolean isChanged = false;
    if (newUse instanceof Immediate) {
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
      }
    }
    if (newUse instanceof Local && v.getBase().equivTo(oldUse)) {
      if (isChanged) {
        newExpr = ((JSpecialInvokeExpr) newExpr).withBase((Local) newUse);
      } else {
        newExpr = v.withBase((Local) newUse);
        isChanged = true;
      }
    }
    if (!isChanged) {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseInterfaceInvokeExpr(@Nonnull JInterfaceInvokeExpr v) {
    boolean isChanged = false;
    if (newUse instanceof Immediate) {
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
      }
    }
    if (newUse instanceof Local && v.getBase().equivTo(oldUse)) {
      if (isChanged) {
        newExpr = ((JSpecialInvokeExpr) newExpr).withBase((Local) newUse);
      } else {
        newExpr = v.withBase((Local) newUse);
        isChanged = true;
      }
    }
    if (!isChanged) {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseCastExpr(@Nonnull JCastExpr v) {
    if (newUse instanceof Immediate && v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseInstanceOfExpr(@Nonnull JInstanceOfExpr v) {
    if (newUse instanceof Immediate && v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNewArrayExpr(@Nonnull JNewArrayExpr v) {
    if (newUse instanceof Immediate && v.getSize().equivTo(oldUse)) {
      newExpr = v.withSize(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseLengthExpr(@Nonnull JLengthExpr v) {
    if (newUse instanceof Immediate && v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNegExpr(@Nonnull JNegExpr v) {
    if (newUse instanceof Immediate && v.getOp().equivTo(oldUse)) {
      newExpr = v.withOp(newUse);
    } else {
      defaultCase(v);
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
      LinkedHashSet<Local> newArgs = new LinkedHashSet<>(argsList);
      v = v.withArgs(newArgs);

      Map<Local, Block> newArgToBlock = new HashMap<>();
      List<Block> blocks = v.getBlocks();
      for (int i = 0; i < v.getArgsSize(); i++) {
        if (i == index) {
          newArgToBlock.put((Local) newUse, phiBlock);
        } else {
          newArgToBlock.put(argsList.get(i), blocks.get(i));
        }
      }
      newExpr = v.withArgToBlockMap(newArgToBlock);
    } else {
      defaultCase(v);
    }
  }

  @Nonnull
  @Override
  public void caseNewExpr(JNewExpr v) {
    defaultCase(v);
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
