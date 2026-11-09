package sootup.apk.frontend.main;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.types.Type;

/**
 * A cast of a dex conversion instruction, which also names the type it converts from, e.g. {@code
 * float-to-int}. Dex constants are untyped: the 32 bits of {@code const} are an int or a float
 * depending on their uses, and a conversion may be the only use that tells. Jimple's cast only
 * keeps the target type, so the source type is kept here for the dex transformers, as Soot's
 * dexpler keeps it in an operation tag. Otherwise it is an ordinary cast.
 */
public class DexCastExpr extends JCastExpr {

  @NonNull private final Type sourceType;

  public DexCastExpr(@NonNull Immediate op, @NonNull Type type, @NonNull Type sourceType) {
    super(op, type);
    this.sourceType = sourceType;
  }

  /** The type the conversion instruction converts from */
  @NonNull
  public Type getSourceType() {
    return sourceType;
  }

  @NonNull
  @Override
  public DexCastExpr withOp(@NonNull Immediate op) {
    return new DexCastExpr(op, getType(), sourceType);
  }

  @NonNull
  @Override
  public DexCastExpr withType(@NonNull Type type) {
    return new DexCastExpr(getOp(), type, sourceType);
  }
}
