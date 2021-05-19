package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.*;
import javax.annotation.Nonnull;

public final class JPhiExpr implements Expr, Copyable {

  private final Local[] args;
  private final HashMap<Stmt, Local> predToArg = new HashMap<>();
  private final HashMap<Local, Stmt> argToPred;
  private Type type = null;

  public JPhiExpr(@Nonnull LinkedHashSet<Local> args, @Nonnull Map<Local, Stmt> argToPred) {
    this.args = args.stream().toArray(Local[]::new);

    this.argToPred = (HashMap<Local, Stmt>) argToPred;

    for (Local arg : args) {
      if (type == null) {
        this.type = arg.getType();
      } else {
        if (!arg.getType().equals(this.type)) {
          throw new RuntimeException("The given args should have the same type!!");
        }
      }
      predToArg.put(argToPred.get(arg), arg);
    }
  }

  @Nonnull
  public Set<Local> getArgs() {
    return new LinkedHashSet<>(Arrays.asList(this.args));
  }

  @Nonnull
  public int getArgsSize() {
    return this.args.length;
  }

  @Nonnull
  public Local getArg(@Nonnull Stmt pred) {
    if (predToArg.get(pred) == null) {
      throw new RuntimeException("There's no matched arg for the given stmt " + pred.toString());
    }
    return this.predToArg.get(pred);
  }

  @Nonnull
  public Local getArg(@Nonnull int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return args[index];
  }

  @Nonnull
  public int getArgIndex(@Nonnull Stmt pred) {
    if (!this.predToArg.keySet().contains(pred)) {
      throw new RuntimeException(
          "The given stmt: " + pred.toString() + " is not contained by PhiExpr!");
    }
    Local arg = predToArg.get(pred);
    int i = 0;
    for (; i < this.args.length; i++) {
      if (this.args[i] == arg) {
        break;
      }
    }
    return i;
  }

  /**
   * @return a list of Preds in which each Pred corresponds to arg from args with the same list
   *     index.
   */
  @Nonnull
  public List<Stmt> getPreds() {
    List<Stmt> preds = new ArrayList<>();
    Arrays.stream(args).forEach(arg -> preds.add(this.argToPred.get(arg)));
    return preds;
  }

  @Nonnull
  public Stmt getPred(@Nonnull Local arg) {
    if (!getArgs().contains(arg)) {
      throw new RuntimeException(
          "The given arg: " + arg.toString() + " is not contained by PhiExpr!");
    }
    return this.argToPred.get(arg);
  }

  @Nonnull
  public Stmt getPred(@Nonnull int index) {
    if (index >= this.getArgsSize()) {
      throw new RuntimeException("The given index is out of the bound!");
    }
    return this.argToPred.get(args[index]);
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
    if (args != null && args.length != 0) {
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
    StringBuilder builder = new StringBuilder();
    builder.append(Jimple.PHI + "(" + this.args[0].toString());
    for (int i = 1; i < getArgsSize(); i++) {
      builder.append(", " + this.args[i].toString());
    }
    builder.append(")");
    return builder.toString();
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ExprVisitor) sw).casePhiExpr(this);
  }

  @Nonnull
  public JPhiExpr withArgs(@Nonnull LinkedHashSet<Local> args) {
    return new JPhiExpr(args, this.argToPred);
  }

  @Nonnull
  public JPhiExpr withArgToPredMap(@Nonnull Map<Local, Stmt> argToPred) {
    return new JPhiExpr((LinkedHashSet<Local>) getArgs(), argToPred);
  }
}
