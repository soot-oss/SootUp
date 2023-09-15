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

package qilin.core.context;

import qilin.core.pag.AllocNode;
import qilin.core.pag.ClassConstantNode;
import qilin.core.pag.StringConstantNode;
import soot.RefType;
import soot.SootMethod;
import soot.Type;

/**
 * Type based context element in the points to analysis.
 */
public class TypeContextElement implements ContextElement {

    private final Type type;

    private TypeContextElement(Type type) {
        this.type = type;
    }

    public static TypeContextElement getTypeContextElement(AllocNode a) {
        SootMethod declaringMethod = a.getMethod();
        Type declType = RefType.v("java.lang.Object");
        if (declaringMethod != null) {
            declType = declaringMethod.getDeclaringClass().getType();
        } else if (a instanceof ClassConstantNode) {
            declType = RefType.v("java.lang.System");
        } else if (a instanceof StringConstantNode) {
            declType = RefType.v("java.lang.String");
        }
        return new TypeContextElement(declType);
    }

    public Type getType() {
        return type;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TypeContextElement other = (TypeContextElement) obj;
        if (type == null) {
            return other.type == null;
        } else {
            return type.equals(other.type);
        }
    }

    public String toString() {
        return "TypeContext: " + type;
    }
}
