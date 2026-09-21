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

import qilin.core.context.Context;

/**
 * Double-dispatch counterpart of {@link PagNode#parameterize(Parameterizer, Context)}: each PAG
 * node kind that knows how to turn itself into a context-sensitive node calls back into the
 * matching overload here, instead of the caller (e.g. {@code CorePTA}) having to test the node's
 * runtime type with an {@code instanceof} cascade. Implemented by {@code qilin.core.CorePTA}.
 */
public interface Parameterizer {
  ContextVarNode parameterize(LocalVarNode vn, Context context);

  FieldRefNode parameterize(FieldRefNode frn, Context context);

  ContextAllocNode parameterize(AllocNode node, Context context);

  ContextField parameterize(FieldValNode fvn, Context context);

  ContextVarNode parameterize(GlobalVarNode gvn, Context context);
}
