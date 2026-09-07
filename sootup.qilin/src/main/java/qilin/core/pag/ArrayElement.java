/* Qilin - a Java Pointer Analysis Framework
 * Copyright (C) 2021-2030 Qilin developers
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3.0 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <https://www.gnu.org/licenses/lgpl-3.0.en.html>.
 */

package qilin.core.pag;

import qilin.util.JavaTypes;
import sootup.core.types.Type;

/**
 * A pseudo-field standing in for "any element of any array", used to model array stores/loads
 * field-insensitively: {@link PAG} holds exactly one {@code ArrayElement} instance (see {@link
 * PAG#getArrayElement()}), shared across every array type in the program. Because a single,
 * parameterless {@code ArrayElement} represents cells of {@code int[]}, {@code String[]}, {@code
 * Foo[]}, ... all merged together, there is no single real element type it could report -- {@link
 * #getType()} deliberately returns {@link JavaTypes#OBJECT} rather than picking one array's element
 * type arbitrarily. This is an intentional precision/scalability trade-off (the same one Spark
 * made), not information lost by accident; a genuinely array-type-sensitive analysis would need
 * per-(allocation-site-or-type) elements instead of this shared singleton.
 *
 * @author Ondrej Lhotak
 */
public class ArrayElement implements SparkField {
  private int number = 0;

  public final int getNumber() {
    return number;
  }

  public final void setNumber(int number) {
    this.number = number;
  }

  @Override
  public Type getType() {
    return JavaTypes.OBJECT;
  }
}
