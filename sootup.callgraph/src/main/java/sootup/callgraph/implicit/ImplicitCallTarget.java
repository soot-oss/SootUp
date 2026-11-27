package sootup.callgraph.implicit;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Hazem Gamal and others
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
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;

/**
 * A method that a call invokes implicitly. Positions denote the values of the call that owns the
 * target: {@link #RECEIVER} is the receiver of the call, other positions are argument indices.
 */
public sealed interface ImplicitCallTarget {

  /** The position of the receiver of a call */
  int RECEIVER = -1;

  /**
   * Calls the method with the given subsignature on the value at the given position, e.g. {@code
   * run()} on the {@link Runnable} that is passed to {@code Executor.execute(Runnable)}.
   *
   * @param position the value on which the method is called
   * @param subSignature the subsignature of the called method
   * @param declaringClass the class in which the called method is looked up, or null to use the
   *     static type of the value
   * @param parameterMapping maps argument indices of the implicit call's source call to parameter
   *     indices of the called method; unmapped parameters receive no value
   */
  record Direct(
      int position,
      @NonNull MethodSubSignature subSignature,
      @Nullable ClassType declaringClass,
      @NonNull Map<Integer, Integer> parameterMapping)
      implements ImplicitCallTarget {

    public Direct {
      parameterMapping = Map.copyOf(parameterMapping);
    }

    public Direct(int position, @NonNull MethodSubSignature subSignature) {
      this(position, subSignature, null, Map.of());
    }
  }

  /**
   * Targets that depend on an earlier call on the value at the given position in the same method,
   * e.g. the {@link Runnable} that was passed to the constructor of a {@link Thread} before {@code
   * start()} is called on it. The positions of the nested targets denote the values of that earlier
   * call.
   *
   * @param position the value on which the earlier call was made
   * @param callSubSignature the subsignature of the earlier call
   * @param targets the targets relative to the earlier call
   */
  record Indirect(
      int position, @NonNull MethodSubSignature callSubSignature, @NonNull List<Direct> targets)
      implements ImplicitCallTarget {

    public Indirect {
      targets = List.copyOf(targets);
    }
  }
}
