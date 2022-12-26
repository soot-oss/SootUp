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

package qilin.core.reflection;

public enum ReflectionKind {
    ClassForName, ClassNewInstance, ConstructorNewInstance, MethodInvoke, FieldSet, FieldGet, MethodGet, ArrayNewInstance,
    ArrayGet, ArraySet, FieldGetName;

    public static ReflectionKind parse(String kindStr) {
        return switch (kindStr) {
            case "Class.forName" -> ClassForName;
            case "Class.newInstance" -> ClassNewInstance;
            case "Constructor.newInstance" -> ConstructorNewInstance;
            case "Method.invoke" -> MethodInvoke;
            case "Method.getName" -> MethodGet;
            case "Field.set*" -> FieldSet;
            case "Field.get*" -> FieldGet;
            case "Field.getName" -> FieldGetName;
            case "Array.newInstance" -> ArrayNewInstance;
            default -> null;
        };
    }
}