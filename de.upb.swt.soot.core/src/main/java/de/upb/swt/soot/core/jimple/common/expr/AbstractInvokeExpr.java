package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Linghui Luo, Markus Schmidt and others
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

import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

public abstract class AbstractInvokeExpr implements Expr {

  private final MethodSignature methodSignature;
  private final ValueBox[] argBoxes;
  // new attribute: later if ValueBox is deleted, then add "final" to it.
  private Value[] args;

  protected AbstractInvokeExpr(MethodSignature method, ValueBox[] argBoxes) {
    this.methodSignature = method;
    this.argBoxes = argBoxes.length == 0 ? null : argBoxes;

    // new attribute: later if ValueBox is deleted, then fit the constructor
    this.args = Arrays.stream(argBoxes).map(ValueBox::getValue).toArray(Value[]::new);
  }

  public MethodSignature getMethodSignature() {
    return this.methodSignature;
  }

  public Value getArg(int index) {
    return argBoxes[index].getValue();
  }

  /** Returns a list of arguments, consisting of values contained in the box. */
  public List<Value> getArgs() {
    return argBoxes != null
        ? Arrays.stream(argBoxes).map(ValueBox::getValue).collect(Collectors.toList())
        : Collections.emptyList();
  }

  @Nullable
  List<ValueBox> getArgBoxes() {
    return Collections.unmodifiableList(Arrays.asList(argBoxes));
  }

  public int getArgCount() {
    return argBoxes == null ? 0 : argBoxes.length;
  }

  public ValueBox getArgBox(int index) {
    return argBoxes[index];
  }

  @Override
  public Type getType() {
    return methodSignature.getType();
  }

  @Override
  public List<Value> getUses() {
    if (args == null) {
      return Collections.emptyList();
    }
    List<Value> list = new ArrayList<>();
    Collections.addAll(list, args);
    for (Value arg : args) {
      list.addAll(arg.getUses());
    }
    return list;
  }

  protected void argBoxesToString(StringBuilder builder) {
    if (argBoxes != null) {
      final int len = argBoxes.length;
      if (0 < len) {
        builder.append(argBoxes[0].getValue().toString());
        for (int i = 1; i < len; i++) {
          builder.append(", ");
          builder.append(argBoxes[i].getValue().toString());
        }
      }
    }
  }

  protected void argBoxesToPrinter(StmtPrinter up) {
    if (argBoxes != null) {
      final int len = argBoxes.length;
      if (0 < len) {
        argBoxes[0].toString(up);
        for (int i = 1; i < len; i++) {
          up.literal(", ");
          argBoxes[i].toString(up);
        }
      }
    }
  }
}
