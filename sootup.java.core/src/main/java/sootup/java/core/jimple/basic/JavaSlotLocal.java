package sootup.java.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2020 Markus Schmidt
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
import sootup.core.jimple.common.SlotLocal;
import sootup.core.types.Type;
import sootup.java.core.AnnotationUsage;

/** Combines Java annotations with original JVM local variable slot provenance. */
public interface JavaSlotLocal extends JavaLocal, SlotLocal {
  @Override
  @NonNull JavaSlotLocal withName(@NonNull String name);

  @Override
  @NonNull JavaSlotLocal withType(@NonNull Type type);

  @Override
  @NonNull JavaSlotLocal withAnnotations(@NonNull Iterable<AnnotationUsage> annotations);

  @Override
  @NonNull JavaSlotLocal withSlotIndex(int slotIndex);

  @Override
  default @NonNull JavaSlotLocal withIndex(int slotIndex) {
    return withSlotIndex(slotIndex);
  }
}
