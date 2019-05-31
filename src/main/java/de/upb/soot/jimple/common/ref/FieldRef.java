/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
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

package de.upb.soot.jimple.common.ref;

import de.upb.soot.core.SootField;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.types.Type;
import de.upb.soot.views.IView;
import java.util.Optional;
import javax.annotation.Nonnull;

public abstract class FieldRef implements ConcreteRef {

  /** */
  private static final long serialVersionUID = 1914104591633719756L;

  protected final FieldSignature fieldSignature;

  protected FieldRef(FieldSignature fieldSignature) {
    this.fieldSignature = fieldSignature;
  }

  public @Nonnull Optional<SootField> getField(@Nonnull IView view) {
    return view.getClass(fieldSignature.getDeclClassSignature())
        .flatMap(it -> it.getField(fieldSignature).map(field -> (SootField) field));
  }

  public FieldSignature getFieldSignature() {
    return this.fieldSignature;
  }

  @Override
  public Type getType() {
    return fieldSignature.getSignature();
  }
}
