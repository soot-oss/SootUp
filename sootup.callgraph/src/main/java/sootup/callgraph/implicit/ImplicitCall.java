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
import org.jspecify.annotations.NonNull;
import sootup.core.signatures.MethodSignature;

/**
 * A rule of an {@link ImplicitCallModel}: calls of the source method implicitly invoke the targets.
 * A static source matches calls of exactly that method. An instance source matches virtual and
 * interface calls with the same subsignature whose referenced class is the source's declaring class
 * or one of its subtypes.
 *
 * @param source the method whose calls perform the implicit calls
 * @param staticSource whether the source is a static method
 * @param targets the implicitly called methods
 */
public record ImplicitCall(
    @NonNull MethodSignature source,
    boolean staticSource,
    @NonNull List<ImplicitCallTarget> targets) {

  public ImplicitCall {
    targets = List.copyOf(targets);
  }
}
