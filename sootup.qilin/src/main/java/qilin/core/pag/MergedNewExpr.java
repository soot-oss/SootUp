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

import java.util.Map;
import qilin.util.DataFactory;
import sootup.core.types.ReferenceType;

public class MergedNewExpr {
  private final ReferenceType type;
  private static final Map<ReferenceType, MergedNewExpr> map = DataFactory.createMap();

  private MergedNewExpr(ReferenceType type) {
    this.type = type;
  }

  public static MergedNewExpr v(ReferenceType type) {
    return map.computeIfAbsent(type, k -> new MergedNewExpr(type));
  }
}
