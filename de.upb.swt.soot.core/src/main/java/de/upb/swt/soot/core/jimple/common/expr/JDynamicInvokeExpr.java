package de.upb.swt.soot.core.jimple.common.expr;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1999-2020 Patrick Lam, Markus Schmidt, Christian Brüggemann and others
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

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.basic.ValueBox;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

public final class JDynamicInvokeExpr extends AbstractInvokeExpr implements Copyable {

  private final MethodSignature bootstrapMethodSignature;
  private final ValueBox[] bootstrapMethodSignatureArgBoxes;
  private final int tag;

  /** Assigns values returned by newImmediateBox to an array bsmArgBoxes of type ValueBox. */
  // TODO: [ms] if we only allow: INVOKEDYNAMIC_DUMMY_CLASS_NAME as class for classSig.. why dont we
  // just want methodsubsignature as parameter?!
  public JDynamicInvokeExpr(
      MethodSignature bootstrapMethodSignature,
      List<? extends Value> bootstrapArgs,
      MethodSignature methodSignature,
      int tag,
      List<? extends Value> methodArgs) {
    super(methodSignature, ValueBoxUtils.toValueBoxes(methodArgs));
    if (!methodSignature
        .toString()
        .startsWith("<" + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME + ": ")) {
      throw new IllegalArgumentException(
          "Receiver type of JDynamicInvokeExpr must be "
              + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME
              + "!");
    }
    this.bootstrapMethodSignature = bootstrapMethodSignature;
    this.bootstrapMethodSignatureArgBoxes = new ValueBox[bootstrapArgs.size()];
    this.tag = tag;

    for (int i = 0; i < bootstrapArgs.size(); i++) {
      this.bootstrapMethodSignatureArgBoxes[i] = Jimple.newImmediateBox(bootstrapArgs.get(i));
    }
  }

  /** Makes a parameterized call to JDynamicInvokeExpr method. */
  public JDynamicInvokeExpr(
      MethodSignature bootstrapMethodSignature,
      List<? extends Value> bootstrapArgs,
      MethodSignature methodSignature,
      List<? extends Value> methodArgs) {
    /*
     * Here the static-handle is chosen as default value, because this works for Java.
     */
    this(
        bootstrapMethodSignature,
        bootstrapArgs,
        methodSignature,
        6, // its Opcodes.H_INVOKESTATIC
        methodArgs);
  }

  public @Nonnull MethodSignature getBootstrapMethodSignature() {
    return this.bootstrapMethodSignature;
  }

  public int getBootstrapArgCount() {
    return bootstrapMethodSignatureArgBoxes.length;
  }

  public Value getBootstrapArg(int index) {
    return bootstrapMethodSignatureArgBoxes[index].getValue();
  }

  @Override
  public boolean equivTo(Object o, JimpleComparator comparator) {
    return comparator.caseDynamicInvokeExpr(this, o);
  }

  /** Returns a hash code for this object, consistent with structural equality. */
  @Override
  public int equivHashCode() {
    return bootstrapMethodSignature.hashCode() * getMethodSignature().hashCode() * 17;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(Jimple.DYNAMICINVOKE);
    builder.append(" \"");
    builder.append(getMethodSignature()); // quoted method name (can be any UTF8 string)
    builder.append("\" <");
    builder.append(getMethodSignature().getSubSignature());
    builder.append(">(");

    argBoxesToString(builder);

    builder.append(") ");
    builder.append(this.getBootstrapMethodSignature());
    builder.append("(");
    final int len = bootstrapMethodSignatureArgBoxes.length;
    if (0 < len) {
      builder.append(bootstrapMethodSignatureArgBoxes[0].getValue().toString());
      for (int i = 1; i < len; i++) {
        builder.append(", ");
        builder.append(bootstrapMethodSignatureArgBoxes[i].getValue().toString());
      }
    }
    builder.append(")");

    return builder.toString();
  }

  @Override
  public void toString(@Nonnull StmtPrinter up) {
    up.literal(Jimple.DYNAMICINVOKE);
    up.literal(
        " \""
            + getMethodSignature().getName()
            + "\" <"
            + getMethodSignature().getSubSignature()
            + ">(");
    argBoxesToPrinter(up);

    up.literal(") ");
    up.methodSignature(bootstrapMethodSignature);
    up.literal("(");
    final int len = bootstrapMethodSignatureArgBoxes.length;
    if (0 < len) {
      bootstrapMethodSignatureArgBoxes[0].toString(up);
      for (int i = 1; i < len; i++) {
        up.literal(", ");
        bootstrapMethodSignatureArgBoxes[i].toString(up);
      }
    }
    up.literal(")");
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ExprVisitor) sw).caseDynamicInvokeExpr(this);
  }

  /** Returns a list containing elements of type ValueBox. */
  public List<Value> getBootstrapArgs() {
    return Arrays.stream(bootstrapMethodSignatureArgBoxes)
        .map(ValueBox::getValue)
        .collect(Collectors.toList());
  }

  public int getHandleTag() {
    return tag;
  }

  @Nonnull
  public JDynamicInvokeExpr withBootstrapMethodSignature(MethodSignature bootstrapMethodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withBootstrapArgs(List<? extends Value> bootstrapArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, bootstrapArgs, getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withMethodSignature(MethodSignature methodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withMethodArgs(List<? extends Value> methodArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), methodArgs);
  }
}
