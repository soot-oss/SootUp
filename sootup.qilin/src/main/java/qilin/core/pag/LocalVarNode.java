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

import soot.SootMethod;
import soot.Type;

/**
 * @author Ondrej Lhotak
 */
public class LocalVarNode extends VarNode {
    protected SootMethod method;

    public LocalVarNode(Object variable, Type t, SootMethod m) {
        super(variable, t);
        this.method = m;
    }

    public SootMethod getMethod() {
        return method;
    }

    public String toString() {
        return "LocalVarNode " + getNumber() + " " + variable + " " + method;
    }

    /**
     * Returns true if this VarNode represents the THIS pointer
     */
    public boolean isThis() {
        if (variable instanceof Parm parm) {
            return parm.isThis();
        }
        return false;
    }

    public boolean isReturn() {
        if (variable instanceof Parm parm) {
            return parm.isReturn();
        }
        return false;
    }

    @Override
    public VarNode base() {
        return this;
    }
}
