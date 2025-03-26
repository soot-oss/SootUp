package sootup.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Linghui Luo, Christian Brüggemann
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
import sootup.core.jimple.basic.LValue;
import sootup.core.signatures.FieldSignature;
import sootup.core.types.Type;

public abstract class JFieldRef implements ConcreteRef, LValue {

  @NonNull private final FieldSignature fieldSignature;

  JFieldRef(@NonNull FieldSignature fieldSignature) {
    this.fieldSignature = fieldSignature;
  }

  @NonNull
  public FieldSignature getFieldSignature() {
    return fieldSignature;
  }

  @NonNull
  @Override
  public Type getType() {
    return fieldSignature.getType();
  }

  public boolean equals(JFieldRef ref) {
    if (this == ref) {
      return true;
    }
    return this.getFieldSignature().equals(ref.getFieldSignature());
  }

  @NonNull
  public abstract JFieldRef withFieldSignature(@NonNull FieldSignature fieldSignature);
}
