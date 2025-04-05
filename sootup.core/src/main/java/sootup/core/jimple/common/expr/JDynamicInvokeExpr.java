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
import java.util.Optional;
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
    return true;
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
    return false;
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
    return this;
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
    return null;
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
    return Optional.of(this);
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
    return Optional.empty();
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
