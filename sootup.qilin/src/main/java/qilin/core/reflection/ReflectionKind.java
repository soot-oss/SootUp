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
  ClassForName,
  ClassNewInstance,
  ConstructorNewInstance,
  MethodInvoke,
  FieldSet,
  FieldGet,
  MethodGet,
  ArrayNewInstance,
  ArrayGet,
  ArraySet,
  FieldGetName;

  public static ReflectionKind parse(String kindStr) {
    switch (kindStr) {
      case "Class.forName":
        return ClassForName;
      case "Class.newInstance":
        return ClassNewInstance;
      case "Constructor.newInstance":
        return ConstructorNewInstance;
      case "Method.invoke":
        return MethodInvoke;
      case "Method.getName":
        return MethodGet;
      case "Field.set*":
        return FieldSet;
      case "Field.get*":
        return FieldGet;
      case "Field.getName":
        return FieldGetName;
      case "Array.newInstance":
        return ArrayNewInstance;
      default:
        return null;
    }
  }
}
