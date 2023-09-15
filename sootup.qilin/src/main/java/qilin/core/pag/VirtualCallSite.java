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

import soot.Kind;
import soot.MethodOrMethodContext;
import soot.jimple.InstanceInvokeExpr;
import soot.jimple.Stmt;
import soot.util.NumberedString;

import java.util.Objects;

/**
 * Holds relevant information about a particular virtual call site.
 *
 * @author Ondrej Lhotak
 */
public class VirtualCallSite extends CallSite {
    private final VarNode recNode;
    private final MethodOrMethodContext container;
    private final InstanceInvokeExpr iie;
    private final NumberedString subSig;
    private final Kind kind;

    public VirtualCallSite(VarNode recNode, Stmt stmt, MethodOrMethodContext container, InstanceInvokeExpr iie,
                           NumberedString subSig, Kind kind) {
        super(stmt);
        this.recNode = recNode;
        this.container = container;
        this.iie = iie;
        this.subSig = subSig;
        this.kind = kind;
    }

    public VarNode recNode() {
        return recNode;
    }

    public MethodOrMethodContext container() {
        return container;
    }

    public InstanceInvokeExpr iie() {
        return iie;
    }

    public NumberedString subSig() {
        return subSig;
    }

    public Kind kind() {
        return kind;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        VirtualCallSite that = (VirtualCallSite) o;
        return recNode.equals(that.recNode) && container.equals(that.container) && iie.equals(that.iie) && subSig.equals(that.subSig) && kind.equals(that.kind);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), recNode, container, iie, subSig, kind);
    }
}
