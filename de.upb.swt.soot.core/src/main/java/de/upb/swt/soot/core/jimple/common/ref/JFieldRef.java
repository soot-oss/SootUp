package de.upb.swt.soot.core.jimple.common.ref;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 * Copyright (C) 2004 Ondrej Lhotak
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

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;
import java.util.Optional;
import javax.annotation.Nonnull;

public abstract class JFieldRef implements ConcreteRef {

  private final FieldSignature fieldSignature;

  JFieldRef(FieldSignature fieldSignature) {
    this.fieldSignature = fieldSignature;
  }

  public @Nonnull Optional<SootField> getField(@Nonnull View view) {
    return view.getClass(fieldSignature.getDeclClassType())
        .flatMap(it -> it.getField(fieldSignature).map(field -> (SootField) field));
  }

  public FieldSignature getFieldSignature() {
    return fieldSignature;
  }

  @Override
  public Type getType() {
    return fieldSignature.getType();
  }
}
