package de.upb.swt.soot.java.bytecode.interceptors.typeassignerutils;
/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2008 Ben Bellamy
 *
 * All rights reserved.
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

import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.UnknownType;

import java.util.Collection;
import java.util.Collections;

/**
 * @author Marcus Nachtigall
 */
public class AugHierarchy implements IHierarchy {
    public Collection<Type> lcas(Type a, Type b) {
        return lcas_(a, b);
    }

    public static Collection<Type> lcas_(Type a, Type b) {
        if (TypeResolver.typesEqual(a, b)) {
            return Collections.singletonList(a);
        } else if (a instanceof UnknownType) {
            return Collections.singletonList(b);
        } else if (b instanceof UnknownType) {
            return Collections.singletonList(a);
        } else if (a instanceof PrimitiveType && b instanceof PrimitiveType) {
            if(PrimitiveType.isInstanceOfType(a, "boolean") || PrimitiveType.isInstanceOfType(b, "boolean")){
                return Collections.emptyList();
            } else if((PrimitiveType.isInstanceOfType(a, "byte") && PrimitiveType.isInstanceOfType(b, "short"))
                || (PrimitiveType.isInstanceOfType(b, "byte") && PrimitiveType.isInstanceOfType(a, "short"))){
                return Collections.singletonList(PrimitiveType.getShort());
            } else if ((PrimitiveType.isInstanceOfType(a, "char") && (PrimitiveType.isInstanceOfType(b, "short") || PrimitiveType.isInstanceOfType(b, "byte")))
                || (PrimitiveType.isInstanceOfType(b, "char") && (PrimitiveType.isInstanceOfType(a, "short") || PrimitiveType.isInstanceOfType(a, "byte")))) {
                return Collections.singletonList(PrimitiveType.getInt());
            } else if (ancestor_(a, b)) {
                return Collections.singletonList(a);
            } else if (PrimitiveType.isInstanceOfType(a, "int") || PrimitiveType.isInstanceOfType(b, "int")){
                return Collections.emptyList();
            }
            else {
                return Collections.singletonList(b);
            }
        } else {
            return BytecodeHierarchy.lcas_(a, b);
        }
    }

    public boolean ancestor(Type ancestor, Type child) {
        return ancestor_(ancestor, child);
    }

    public static boolean ancestor_(Type ancestor, Type child) {
        if (TypeResolver.typesEqual(ancestor, child)) {
            return true;
        } else if (PrimitiveType.isInstanceOfType(ancestor, "int")) {
            return child instanceof UnknownType;
        } else if (PrimitiveType.isInstanceOfType(ancestor, "boolean")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "boolean");
        } else if (PrimitiveType.isInstanceOfType(ancestor, "short")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "boolean");
        } else if (PrimitiveType.isInstanceOfType(ancestor, "byte") || PrimitiveType.isInstanceOfType(ancestor, "short")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "boolean") || PrimitiveType.isInstanceOfType(child, "short");
        } else if (PrimitiveType.isInstanceOfType(ancestor, "char")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "boolean") || PrimitiveType.isInstanceOfType(child, "byte")
                || PrimitiveType.isInstanceOfType(child, "short");
        } else if (PrimitiveType.isInstanceOfType(ancestor, "short")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "boolean") || PrimitiveType.isInstanceOfType(child, "short")
                || PrimitiveType.isInstanceOfType(child, "byte");
        } else if (PrimitiveType.isInstanceOfType(ancestor, "int")) {
            return child instanceof UnknownType || PrimitiveType.isInstanceOfType(child, "int") || PrimitiveType.isInstanceOfType(child, "byte") || PrimitiveType.isInstanceOfType(child, "char")
                || PrimitiveType.isInstanceOfType(child, "short");
        } else if (PrimitiveType.isInstanceOfType(child, "int")) {
            return false;
        } else {
            return BytecodeHierarchy.ancestor_(ancestor, child);
        }
    }

}
