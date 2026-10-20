package sootup.callgraph.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import org.jspecify.annotations.NonNull;
import sootup.core.jimple.common.constant.MethodHandle;
import sootup.core.signatures.MethodSignature;

/**
 * A method an invokedynamic call site transfers control to.
 *
 * @param method the referenced method, as written in the method handle (not dispatched yet)
 * @param kind how the method handle invokes {@code method}
 * @param lambdaImplementation whether {@code method} implements a {@code LambdaMetafactory} call
 *     site, i.e. the call site's arguments are values captured for it
 */
public record DynamicInvokeTarget(
    @NonNull MethodSignature method,
    MethodHandle.@NonNull Kind kind,
    boolean lambdaImplementation) {

  /** {@link #captureParameterIndex(int)} result for a captured receiver. */
  public static final int RECEIVER = -1;

  /** {@link #captureParameterIndex(int)} result for an argument that does not reach the target. */
  public static final int NOT_BOUND = -2;

  /**
   * Where the invokedynamic's {@code argIndex}-th argument lands in {@link #method()}: {@link
   * #RECEIVER} for a captured receiver ({@code obj::m}, or {@code this} of an instance lambda
   * body), else the parameter index - captured values precede the functional interface's own
   * arguments. {@link #NOT_BOUND} unless this is a {@link #lambdaImplementation()}.
   */
  public int captureParameterIndex(int argIndex) {
    if (!lambdaImplementation) {
      return NOT_BOUND;
    }
    if (!hasReceiver()) {
      return argIndex;
    }
    return argIndex == 0 ? RECEIVER : argIndex - 1;
  }

  /** Whether {@link #method()} is invoked on a receiver object (not static, not a constructor). */
  public boolean hasReceiver() {
    return kind == MethodHandle.Kind.REF_INVOKE_VIRTUAL
        || kind == MethodHandle.Kind.REF_INVOKE_INTERFACE
        || kind == MethodHandle.Kind.REF_INVOKE_SPECIAL;
  }
}
