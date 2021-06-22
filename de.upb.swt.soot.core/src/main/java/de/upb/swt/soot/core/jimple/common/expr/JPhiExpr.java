package de.upb.swt.soot.core.jimple.common.expr;

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

import de.upb.swt.soot.core.graph.Block;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.*;
import javax.annotation.Nonnull;

/** @Zun Wang */
public final class JPhiExpr implements Expr, Copyable {

  private List<Local> args = new ArrayList<>();
  private Map<Block, Local> blockToArg = new HashMap<>();
  private Map<Local, Block> argToBlock = new HashMap<>();
  private Type type = null;

  public JPhiExpr(@Nonnull List<Local> args, @Nonnull Map<Local, Block> argToBlock) {
    this.args = args;

    this.argToBlock = argToBlock;

    for (Local arg : args) {
      if (type == null) {
        this.type = arg.getType();
      } else {
        if (!arg.getType().equals(this.type)) {
          throw new RuntimeException("The given args should have the same type!!");
        }
      }
      blockToArg.put(argToBlock.get(arg), arg);
    }
  }

  private JPhiExpr() {}

  public static JPhiExpr getEmptyPhi() {
    return new JPhiExpr();
  }

  public void addArg(Local arg, Block block) {
    // TODO: now there's validation test
    this.args.add(arg);
    this.blockToArg.put(block, arg);
    this.argToBlock.put(arg, block);
    if (this.type == null) {
      this.type = arg.getType();
    }
  }

  @Nonnull
  public List<Local> getArgs() {
    return this.args;
  }

  @Nonnull
  public int getArgsSize() {
    return this.args.size();
  }

  @Nonnull
  public Local getArg(@Nonnull Block block) {
    if (blockToArg.get(block) == null) {
      throw new RuntimeException("There's no matched arg for the given block " + block.toString());
    }
    return this.blockToArg.get(block);
  }

  @Nonnull
  public Local getArg(@Nonnull int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    List<Local> argsList = new ArrayList<>(this.args);
    return argsList.get(index);
  }

  @Nonnull
  public int getArgIndex(@Nonnull Block block) {
    if (!this.blockToArg.keySet().contains(block)) {
      throw new RuntimeException(
          "The given block: " + block.toString() + " is not contained by PhiExpr!");
    }
    Local arg = blockToArg.get(block);
    List<Local> argsList = new ArrayList<>(this.args);
    return argsList.indexOf(arg);
  }

  /**
   * @return a list of Preds in which each Pred corresponds to arg from args with the same list
   *     index.
   */
  @Nonnull
  public List<Block> getBlocks() {
    List<Block> blocks = new ArrayList<>();
    this.args.forEach(arg -> blocks.add(this.argToBlock.get(arg)));
    return blocks;
  }

  @Nonnull
  public Block getBlock(@Nonnull Local arg) {
    if (!getArgs().contains(arg)) {
      throw new RuntimeException(
          "The given arg: " + arg.toString() + " is not contained by PhiExpr!");
    }
    return this.argToBlock.get(arg);
  }

  @Nonnull
  public Block getBlock(@Nonnull int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return this.argToBlock.get(getArg(index));
  }

  @Override
  public List<Value> getUses() {
    if (args == null) {
      return Collections.emptyList();
    }
    List<Value> list = new ArrayList<>(getArgs());
    return list;
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(args);
  }

  @Override
  public boolean equivTo(Object o, @Nonnull JimpleComparator comparator) {
    return comparator.caseJPhiExpr(this, o);
  }

  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.PHI);
    up.literal("(");
    if (args != null && args.size() != 0) {
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
    if (this.args.size() == 0) {
      return Jimple.PHI + "()";
    }
    StringBuilder builder = new StringBuilder();
    ArrayList<Local> argsList = new ArrayList<>(this.args);
    builder.append(Jimple.PHI + "(" + argsList.get(0).toString());
    for (int i = 1; i < getArgsSize(); i++) {
      builder.append(", " + argsList.get(i).toString());
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ExprVisitor) sw).casePhiExpr(this);
  }

  @Nonnull
  public JPhiExpr withArgs(@Nonnull List<Local> args) {
    return new JPhiExpr(args, this.argToBlock);
  }

  @Nonnull
  public JPhiExpr withArgToBlockMap(@Nonnull Map<Local, Block> argToBlock) {
    return new JPhiExpr(getArgs(), argToBlock);
  }
}
