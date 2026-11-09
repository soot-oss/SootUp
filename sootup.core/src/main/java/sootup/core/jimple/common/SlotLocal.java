package sootup.core.jimple.common;

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

import org.jspecify.annotations.NonNull;
import sootup.core.types.Type;

/**
 * A local originating from a JVM local variable slot. The index is original bytecode provenance.
 */
public interface SlotLocal extends Local {
  /** Returns the original, nonnegative JVM local variable slot index. */
  int getSlotIndex();

  /** Returns a copy with a nonnegative slot index, preserving all other state and capabilities. */
  @NonNull SlotLocal withSlotIndex(int slotIndex);

  @Override
  @NonNull SlotLocal withName(@NonNull String name);

  @Override
  @NonNull SlotLocal withType(@NonNull Type type);

  /** Alias for {@link #getSlotIndex()}. */
  default int getIndex() {
    return getSlotIndex();
  }

  /** Alias for {@link #withSlotIndex(int)}. */
  default @NonNull SlotLocal withIndex(int slotIndex) {
    return withSlotIndex(slotIndex);
  }
}
