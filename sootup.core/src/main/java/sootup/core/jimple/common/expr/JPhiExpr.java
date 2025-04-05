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
import java.util.stream.Stream;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.graph.BasicBlock;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.types.Type;
import sootup.core.util.printer.StmtPrinter;

/**
 * @author Zun Wang
 */
public final class JPhiExpr implements Expr {

  private final List<Local> args;
  private final Map<BasicBlock<?>, Local> blockToArg = new HashMap<>();
  private final Map<Local, BasicBlock<?>> argToBlock;
  @Nullable private final Type type;

  public JPhiExpr(@NonNull List<Local> args, @NonNull Map<Local, BasicBlock<?>> argToBlock) {
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

  @NonNull
  public List<Local> getArgs() {
    return new ArrayList<>(this.args);
  }

  public int getArgsSize() {
    return this.args.size();
  }

  @NonNull
  public Local getArg(@NonNull BasicBlock<?> block) {
    if (blockToArg.get(block) == null) {
      throw new RuntimeException("There's no matched arg for the given block " + block);
    }
    return this.blockToArg.get(block);
  }

  @NonNull
  public Local getArg(int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return args.get(index);
  }

  public int getArgIndex(@NonNull BasicBlock<?> block) {
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
  @NonNull
  public List<BasicBlock<?>> getBlocks() {
    List<BasicBlock<?>> blocks = new ArrayList<>();
    this.args.forEach(arg -> blocks.add(this.argToBlock.get(arg)));
    return blocks;
  }

  @NonNull
  public BasicBlock<?> getBlock(@NonNull Local arg) {
    if (!getArgs().contains(arg)) {
      throw new RuntimeException(
          "The given arg: " + arg.toString() + " is not contained by PhiExpr!");
    }
    return this.argToBlock.get(arg);
  }

  @NonNull
  public BasicBlock<?> getBlock(int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return this.argToBlock.get(getArg(index));
  }

  @NonNull
  public Map<Local, BasicBlock<?>> getArgToBlockMap() {
    return new HashMap<>(this.argToBlock);
  }

  @NonNull
  @Override
  public Stream<Value> getUses() {
    if (args == null) {
      return Stream.empty();
    }
    return getArgs().stream().map(v -> v);
  }

  @Override
  public int equivHashCode() {
    return Objects.hash(args);
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
    return comparator.caseJPhiExpr(this, o);
  }

  @NonNull
  @Override
  public Type getType() {
    return this.type;
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
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

  @NonNull
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

  @NonNull
  public JPhiExpr withArgs(@NonNull List<Local> args) {
    return new JPhiExpr(args, this.argToBlock);
  }

  @NonNull
  public JPhiExpr withArgToBlockMap(@NonNull Map<Local, BasicBlock<?>> argToBlock) {
    return new JPhiExpr(getArgs(), argToBlock);
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.casePhiExpr(this);
    return v;
  }

  @Override
  public boolean isJAddExpr() {
    return false;
  }

  @Override
  public boolean isJAndExpr() {
    return false;
  }

  @Override
  public boolean isJCastExpr() {
    return false;
  }

  @Override
  public boolean isJCmpExpr() {
    return false;
  }

  @Override
  public boolean isJCmpgExpr() {
    return false;
  }

  @Override
  public boolean isJCmplExpr() {
    return false;
  }

  @Override
  public boolean isJDivExpr() {
    return false;
  }

  @Override
  public boolean isJDynamicInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJEqExpr() {
    return false;
  }

  @Override
  public boolean isJGeExpr() {
    return false;
  }

  @Override
  public boolean isJGtExpr() {
    return false;
  }

  @Override
  public boolean isJInstanceOfExpr() {
    return false;
  }

  @Override
  public boolean isJInterfaceInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJLeExpr() {
    return false;
  }

  @Override
  public boolean isJLengthExpr() {
    return false;
  }

  @Override
  public boolean isJLtExpr() {
    return false;
  }

  @Override
  public boolean isJMulExpr() {
    return false;
  }

  @Override
  public boolean isJNeExpr() {
    return false;
  }

  @Override
  public boolean isJNegExpr() {
    return false;
  }

  @Override
  public boolean isJNewArrayExpr() {
    return false;
  }

  @Override
  public boolean isJNewExpr() {
    return false;
  }

  @Override
  public boolean isJNewMultiArrayExpr() {
    return false;
  }

  @Override
  public boolean isJOrExpr() {
    return false;
  }

  @Override
  public boolean isJPhiExpr() {
    return true;
  }

  @Override
  public boolean isJRemExpr() {
    return false;
  }

  @Override
  public boolean isJShlExpr() {
    return false;
  }

  @Override
  public boolean isJShrExpr() {
    return false;
  }

  @Override
  public boolean isJSpecialInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJStaticInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJSubExpr() {
    return false;
  }

  @Override
  public boolean isJUshrExpr() {
    return false;
  }

  @Override
  public boolean isJVirtualInvokeExpr() {
    return false;
  }

  @Override
  public boolean isJXorExpr() {
    return false;
  }

  @Override
  public JAddExpr asJAddExpr() {
    return null;
  }

  @Override
  public JAndExpr asJAndExpr() {
    return null;
  }

  @Override
  public JCastExpr asJCastExpr() {
    return null;
  }

  @Override
  public JCmpExpr asJCmpExpr() {
    return null;
  }

  @Override
  public JCmpgExpr asJCmpgExpr() {
    return null;
  }

  @Override
  public JCmplExpr asJCmplExpr() {
    return null;
  }

  @Override
  public JDivExpr asJDivExpr() {
    return null;
  }

  @Override
  public JDynamicInvokeExpr asJDynamicInvokeExpr() {
    return null;
  }

  @Override
  public JEqExpr asJEqExpr() {
    return null;
  }

  @Override
  public JGeExpr asJGeExpr() {
    return null;
  }

  @Override
  public JGtExpr asJGtExpr() {
    return null;
  }

  @Override
  public JInstanceOfExpr asJInstanceOfExpr() {
    return null;
  }

  @Override
  public JInterfaceInvokeExpr asJInterfaceInvokeExpr() {
    return null;
  }

  @Override
  public JLeExpr asJLeExpr() {
    return null;
  }

  @Override
  public JLengthExpr asJLengthExpr() {
    return null;
  }

  @Override
  public JLtExpr asJLtExpr() {
    return null;
  }

  @Override
  public JMulExpr asJMulExpr() {
    return null;
  }

  @Override
  public JNeExpr asJNeExpr() {
    return null;
  }

  @Override
  public JNegExpr asJNegExpr() {
    return null;
  }

  @Override
  public JNewArrayExpr asJNewArrayExpr() {
    return null;
  }

  @Override
  public JNewExpr asJNewExpr() {
    return null;
  }

  @Override
  public JNewMultiArrayExpr asJNewMultiArrayExpr() {
    return null;
  }

  @Override
  public JOrExpr asJOrExpr() {
    return null;
  }

  @Override
  public JPhiExpr asJPhiExpr() {
    return this;
  }

  @Override
  public JRemExpr asJRemExpr() {
    return null;
  }

  @Override
  public JShlExpr asJShlExpr() {
    return null;
  }

  @Override
  public JShrExpr asJShrExpr() {
    return null;
  }

  @Override
  public JSpecialInvokeExpr asJSpecialInvokeExpr() {
    return null;
  }

  @Override
  public JStaticInvokeExpr asJStaticInvokeExpr() {
    return null;
  }

  @Override
  public JSubExpr asJSubExpr() {
    return null;
  }

  @Override
  public JUshrExpr asJUshrExpr() {
    return null;
  }

  @Override
  public JVirtualInvokeExpr asJVirtualInvokeExpr() {
    return null;
  }

  @Override
  public JXorExpr asJXorExpr() {
    return null;
  }

  @Override
  public Optional<JAddExpr> toJAddExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JAndExpr> toJAndExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCastExpr> toJCastExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmpExpr> toJCmpExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmpgExpr> toJCmpgExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JCmplExpr> toJCmplExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JDivExpr> toJDivExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JDynamicInvokeExpr> toJDynamicInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JEqExpr> toJEqExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JGeExpr> toJGeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JGtExpr> toJGtExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JInstanceOfExpr> toJInstanceOfExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JInterfaceInvokeExpr> toJInterfaceInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLeExpr> toJLeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLengthExpr> toJLengthExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JLtExpr> toJLtExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JMulExpr> toJMulExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNeExpr> toJNeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNegExpr> toJNegExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNewArrayExpr> toJNewArrayExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNewExpr> toJNewExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JNewMultiArrayExpr> toJNewMultiArrayExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JOrExpr> toJOrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JPhiExpr> toJPhiExpr() {
    return Optional.of(this);
  }

  @Override
  public Optional<JRemExpr> toJRemExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JShlExpr> toJShlExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JShrExpr> toJShrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JSpecialInvokeExpr> toJSpecialInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JStaticInvokeExpr> toJStaticInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JSubExpr> toJSubExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JUshrExpr> toJUshrExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JVirtualInvokeExpr> toJVirtualInvokeExpr() {
    return Optional.empty();
  }

  @Override
  public Optional<JXorExpr> toJXorExpr() {
    return Optional.empty();
  }
}
