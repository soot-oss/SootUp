/* Soot - a J*va Optimization Framework
 * Copyright (C) 1999 Patrick Lam
 * Copyright (C) 2004 Ondrej Lhotak
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

package de.upb.swt.soot.core.jimple.common.expr;

import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Immediate;
import de.upb.swt.soot.core.jimple.basic.JimpleComparator;
import de.upb.swt.soot.core.jimple.visitor.ExprVisitor;
import de.upb.swt.soot.core.jimple.visitor.Visitor;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.util.Copyable;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.core.util.printer.StmtPrinter;
import java.util.List;
import javax.annotation.Nonnull;

public final class JDynamicInvokeExpr extends AbstractInvokeExpr implements Copyable {

  private final MethodSignature bootstrapMethodSignature;
  private final List<Immediate> bootstrapMethodSignatureArgs;
  private final int tag;

  public JDynamicInvokeExpr(
      @Nonnull MethodSignature bootstrapMethodSignature,
      @Nonnull List<Immediate> bootstrapArgs,
      @Nonnull MethodSignature methodSignature,
      @Nonnull int tag,
      @Nonnull List<Immediate> methodArgs) {
    super(methodSignature, methodArgs);
    if (!methodSignature
        .toString()
        .startsWith("<" + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME + ": ")) {
      throw new IllegalArgumentException(
          "Receiver type of JDynamicInvokeExpr must be "
              + SootClass.INVOKEDYNAMIC_DUMMY_CLASS_NAME
              + "!");
    }
    this.bootstrapMethodSignature = bootstrapMethodSignature;
    this.bootstrapMethodSignatureArgs = ImmutableUtils.immutableListOf(bootstrapArgs);
    this.tag = tag;
  }

  /** Makes a parameterized call to JDynamicInvokeExpr method. */
  public JDynamicInvokeExpr(
      @Nonnull MethodSignature bootstrapMethodSignature,
      @Nonnull List<Immediate> bootstrapArgs,
      @Nonnull MethodSignature methodSignature,
      @Nonnull List<Immediate> methodArgs) {
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

  @Nonnull
  public MethodSignature getBootstrapMethodSignature() {
    return this.bootstrapMethodSignature;
  }

  public int getBootstrapArgCount() {
    return bootstrapMethodSignatureArgs.size();
  }

  @Nonnull
  public Immediate getBootstrapArg(@Nonnull int index) {
    return bootstrapMethodSignatureArgs.get(index);
  }

  @Override
  public boolean equivTo(@Nonnull Object o, @Nonnull JimpleComparator comparator) {
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

    argsToString(builder);

    builder.append(") ");
    builder.append(this.getBootstrapMethodSignature());
    builder.append("(");
    final int len = bootstrapMethodSignatureArgs.size();
    if (0 < len) {
      builder.append(bootstrapMethodSignatureArgs.get(0).toString());
      for (int i = 1; i < len; i++) {
        builder.append(", ");
        builder.append(bootstrapMethodSignatureArgs.get(i).toString());
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
    argsToPrinter(up);

    up.literal(") ");
    up.methodSignature(bootstrapMethodSignature);
    up.literal("(");
    final int len = bootstrapMethodSignatureArgs.size();
    if (0 < len) {
      bootstrapMethodSignatureArgs.get(0).toString(up);
      for (int i = 1; i < len; i++) {
        up.literal(", ");
        bootstrapMethodSignatureArgs.get(i).toString(up);
      }
    }
    up.literal(")");
  }

  @Override
  public void accept(@Nonnull Visitor sw) {
    ((ExprVisitor) sw).caseDynamicInvokeExpr(this);
  }

  /** Returns a list args of type Value. */
  @Nonnull
  public List<Immediate> getBootstrapArgs() {
    return bootstrapMethodSignatureArgs;
  }

  public int getHandleTag() {
    return tag;
  }

  @Nonnull
  public JDynamicInvokeExpr withBootstrapMethodSignature(
      @Nonnull MethodSignature bootstrapMethodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withBootstrapArgs(@Nonnull List<Immediate> bootstrapArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, bootstrapArgs, getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withMethodSignature(@Nonnull MethodSignature methodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @Nonnull
  public JDynamicInvokeExpr withMethodArgs(@Nonnull List<Immediate> methodArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), methodArgs);
  }
}
