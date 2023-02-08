package sootup.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2021 Raja Vallée-Rai, Christian Brüggemann, Zun Wang
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

import java.util.*;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.Type;
import sootup.core.util.Copyable;
import sootup.core.util.printer.StmtPrinter;

/** @author Zun Wang */
public final class JPhiExpr implements Expr, Copyable {

  private final List<Local> args;
  private final Map<BasicBlock<?>, Local> blockToArg = new HashMap<>();
  private final Map<Local, BasicBlock<?>> argToBlock;
  @Nullable private final Type type;

  public JPhiExpr(@Nonnull List<Local> args, @Nonnull Map<Local, BasicBlock<?>> argToBlock) {
    this.args = args;
    this.argToBlock = argToBlock;

    int argsSize = args.size();
    if (argsSize > 0) {
      this.type = args.get(0).getType();
      for (int i = 1; i < argsSize; i++) {
        Local arg = args.get(i);
        if (!arg.getType().equals(this.type)) {
          throw new RuntimeException("The given args should have the same type!");
        }
        blockToArg.put(argToBlock.get(arg), arg);
      }
    } else {
      type = null;
    }
  }

  @Nonnull
  public List<Local> getArgs() {
    return new ArrayList<>(this.args);
  }

  public int getArgsSize() {
    return this.args.size();
  }

  @Nonnull
  public Local getArg(@Nonnull BasicBlock<?> block) {
    if (blockToArg.get(block) == null) {
      throw new RuntimeException("There's no matched arg for the given block " + block);
    }
    return this.blockToArg.get(block);
  }

  @Nonnull
  public Local getArg(int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return args.get(index);
  }

  @Nonnull
  public int getArgIndex(@Nonnull BasicBlock<?> block) {
    if (!this.blockToArg.containsKey(block)) {
      throw new RuntimeException("The given block: " + block + " is not contained by PhiExpr!");
    }
    Local arg = blockToArg.get(block);
    return args.indexOf(arg);
  }

  /**
   * @return a list of Preds in which each Pred corresponds to arg from args with the same list
   *     index.
   */
  @Nonnull
  public List<BasicBlock<?>> getBlocks() {
    List<BasicBlock<?>> blocks = new ArrayList<>();
    this.args.forEach(arg -> blocks.add(this.argToBlock.get(arg)));
    return blocks;
  }

  @Nonnull
  public BasicBlock<?> getBlock(@Nonnull Local arg) {
    if (!getArgs().contains(arg)) {
      throw new RuntimeException(
          "The given arg: " + arg.toString() + " is not contained by PhiExpr!");
    }
    return this.argToBlock.get(arg);
  }

  @Nonnull
  public BasicBlock<?> getBlock(int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return this.argToBlock.get(getArg(index));
  }

  @Nonnull
  public Map<Local, BasicBlock<?>> getArgToBlockMap() {
    return new HashMap<>(this.argToBlock);
  }

  @Nonnull
  @Override
  public List<Value> getUses() {
    if (args == null) {
      return Collections.emptyList();
    }
    return new ArrayList<>(getArgs());
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(args);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseJPhiExpr(this, o);
  }

  @Nonnull
  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.PHI);
    up.literal("(");
    if (args != null && !args.isEmpty()) {
      ArrayList<Local> list = new ArrayList<>(getArgs());
      list.remove(0).toString(up);
      for (Local arg : list) {
        up.literal(", ");
        arg.toString(up);
      }
    }
    up.literal(")");
  }

  @Nonnull
  public String toString() {
    if (this.args.isEmpty()) {
      return Jimple.PHI + "()";
    }
    StringBuilder builder = new StringBuilder();
    ArrayList<Local> argsList = new ArrayList<>(this.args);
    builder.append(Jimple.PHI + "(").append(argsList.get(0).toString());
    for (int i = 1; i < getArgsSize(); i++) {
      builder.append(", ").append(argsList.get(i).toString());
    }
    builder.append(")");
    return builder.toString();
  }

  @Nonnull
  public JPhiExpr withArgs(@Nonnull List<Local> args) {
    return new JPhiExpr(args, this.argToBlock);
  }

  @Nonnull
  public JPhiExpr withArgToBlockMap(@Nonnull Map<Local, BasicBlock<?>> argToBlock) {
    return new JPhiExpr(getArgs(), argToBlock);
  }

  @Override
  public void accept(@Nonnull ExprVisitor v) {
    v.casePhiExpr(this);
  }
}
