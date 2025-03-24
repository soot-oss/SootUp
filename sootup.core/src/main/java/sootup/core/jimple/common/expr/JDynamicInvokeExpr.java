package sootup.core.jimple.common.expr;

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

import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.Immediate;
import sootup.core.jimple.basic.JimpleComparator;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.visitor.ExprVisitor;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.util.ImmutableUtils;
import sootup.core.util.printer.StmtPrinter;

public final class JDynamicInvokeExpr extends AbstractInvokeExpr {

  @NonNull public static final String INVOKEDYNAMIC_DUMMY_CLASS_NAME = "sootup.dummy.InvokeDynamic";
  @NonNull private final MethodSignature bootstrapMethodSignature;
  // TODO: use immutable List?
  private final List<Immediate> bootstrapMethodSignatureArgs;
  private final int tag;

  /** Assigns values returned by newImmediateBox to an array bsmArgBoxes of type ValueBox. */
  // TODO: [ms] if we only allow: INVOKEDYNAMIC_DUMMY_CLASS_NAME as class for classSig.. why dont we
  // just want methodsubsignature as parameter?!
  public JDynamicInvokeExpr(
      @NonNull MethodSignature bootstrapMethodSignature,
      @NonNull List<Immediate> bootstrapArgs,
      @NonNull MethodSignature methodSignature,
      int tag,
      @NonNull List<Immediate> methodArgs) {
    super(methodSignature, methodArgs.toArray(new Immediate[0]));
    if (!methodSignature
        .getDeclClassType()
        .getFullyQualifiedName()
        .equals(INVOKEDYNAMIC_DUMMY_CLASS_NAME)) {
      throw new IllegalArgumentException(
          "Receiver type of JDynamicInvokeExpr must be " + INVOKEDYNAMIC_DUMMY_CLASS_NAME + "!");
    }
    this.bootstrapMethodSignature = bootstrapMethodSignature;
    this.bootstrapMethodSignatureArgs = ImmutableUtils.immutableListOf(bootstrapArgs);
    this.tag = tag;
  }

  /** Makes a parameterized call to JDynamicInvokeExpr method. */
  public JDynamicInvokeExpr(
      @NonNull MethodSignature bootstrapMethodSignature,
      @NonNull List<Immediate> bootstrapArgs,
      @NonNull MethodSignature methodSignature,
      @NonNull List<Immediate> methodArgs) {
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

  @NonNull
  public MethodSignature getBootstrapMethodSignature() {
    return this.bootstrapMethodSignature;
  }

  public int getBootstrapArgCount() {
    return bootstrapMethodSignatureArgs.size();
  }

  @NonNull
  public Value getBootstrapArg(int index) {
    return bootstrapMethodSignatureArgs.get(index);
  }

  @Override
  public boolean equivTo(Object o, @NonNull JimpleComparator comparator) {
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
    builder.append(getMethodSignature().getName()); // quoted method name (can be any UTF8 string)
    builder.append("\" <");
    builder.append(getNamelessSubSig(getMethodSignature().getSubSignature()));
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
    builder.append(')');

    return builder.toString();
  }

  @Override
  public void toString(@NonNull StmtPrinter up) {
    up.literal(Jimple.DYNAMICINVOKE);
    final MethodSignature methodSignature = getMethodSignature();

    final MethodSubSignature mSubSig = methodSignature.getSubSignature();
    // dont print methodname from methodsubsignature in the usual way
    up.literal(
        " \"" + Jimple.escape(mSubSig.getName()) + "\" <" + getNamelessSubSig(mSubSig) + ">(");
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

  @NonNull
  private String getNamelessSubSig(MethodSubSignature mSubSig) {
    return mSubSig.getType()
        + " ("
        + mSubSig.getParameterTypes().stream()
            .map(Object::toString)
            .collect(Collectors.joining(","))
        + ")";
  }

  @Override
  public <V extends ExprVisitor> V accept(@NonNull V v) {
    v.caseDynamicInvokeExpr(this);
    return v;
  }

  /** Returns a list args of type Value. */
  @NonNull
  public List<Immediate> getBootstrapArgs() {
    return bootstrapMethodSignatureArgs;
  }

  public int getHandleTag() {
    return tag;
  }

  @NonNull
  public JDynamicInvokeExpr withBootstrapMethodSignature(
      @NonNull MethodSignature bootstrapMethodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @NonNull
  public JDynamicInvokeExpr withBootstrapArgs(@NonNull List<Immediate> bootstrapArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, bootstrapArgs, getMethodSignature(), getArgs());
  }

  @NonNull
  public JDynamicInvokeExpr withMethodSignature(@NonNull MethodSignature methodSignature) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), getArgs());
  }

  @NonNull
  public JDynamicInvokeExpr withMethodArgs(@NonNull List<Immediate> methodArgs) {
    return new JDynamicInvokeExpr(
        bootstrapMethodSignature, getBootstrapArgs(), getMethodSignature(), methodArgs);
  }
}
