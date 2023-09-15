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

import soot.Context;

public class ContextAllocNode extends AllocNode {
    private final Context context;
    private final AllocNode base;

    public ContextAllocNode(AllocNode base, Context context) {
        super(base.getNewExpr(), base.getType(), base.getMethod());
        this.context = context;
        this.base = base;
    }

    public Context context() {
        return context;
    }

    @Override
    public AllocNode base() {
        return base;
    }

    public String toString() {
        return "ContextAllocNode " + getNumber() + "(" + base + ", " + context + ")";
    }

}
