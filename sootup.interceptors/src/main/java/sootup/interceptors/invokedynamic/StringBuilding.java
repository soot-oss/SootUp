package sootup.interceptors.invokedynamic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt
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

import java.util.List;
import org.jspecify.annotations.Nullable;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.common.Immediate;
import sootup.core.jimple.common.LValue;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.NullConstant;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.Type;
import sootup.core.types.VoidType;

/**
 * Builds a string with a {@code java.lang.StringBuilder}, appending values the way {@code
 * String.valueOf} renders them. An object of a class type is rendered by an explicit null check and
 * {@code toString()} declared by its static type (exactly {@code String.valueOf(Object)}), so call
 * graphs see which {@code toString()} is called.
 */
final class StringBuilding {

  private final Fragment code;
  private final ClassType builderType;
  private final ClassType stringType;
  private final Local builder;

  StringBuilding(Fragment code) {
    this.code = code;
    this.builderType = code.classType("java.lang.StringBuilder");
    this.stringType = code.classType("java.lang.String");
    this.builder = code.newLocal(builderType);
    code.assign(builder, Jimple.newNewExpr(builderType));
    code.invoke(
        code.specialCall(
            builder, builderType, "<init>", VoidType.getInstance(), List.of(), List.of()));
  }

  ClassType stringType() {
    return stringType;
  }

  void appendText(String text) {
    if (!text.isEmpty()) {
      append(stringType, new StringConstant(text, stringType));
    }
  }

  /** Appends {@code value} of static type {@code type} as {@code String.valueOf} renders it. */
  void appendValue(Immediate value, Type type) {
    if (type instanceof PrimitiveType primitive) {
      append(appendParameter(primitive), value);
    } else if (type.equals(stringType)) {
      append(stringType, value);
    } else if (type instanceof ClassType classType && value instanceof Local local) {
      append(stringType, valueOf(local, classType));
    } else {
      append(code.classType("java.lang.Object"), value);
    }
  }

  /**
   * {@code s = v == null ? "null" : v.toString()}, with {@code toString()} declared by {@code
   * type}.
   */
  private Local valueOf(Local value, ClassType type) {
    Local string = code.newLocal(stringType);
    Fragment.Label nonNull = code.newLabel();
    Fragment.Label done = code.newLabel();
    code.ifGoto(Jimple.newNeExpr(value, NullConstant.getInstance()), nonNull);
    code.assign(string, new StringConstant("null", stringType));
    code.jump(done);
    code.bind(nonNull);
    code.assign(
        string, code.instanceCall(value, type, "toString", stringType, List.of(), List.of()));
    code.bind(done);
    return string;
  }

  private void append(Type parameter, Immediate value) {
    code.invoke(
        code.instanceCall(
            builder, builderType, "append", builderType, List.of(parameter), List.of(value)));
  }

  /** The {@code StringBuilder.append} overload for a primitive (byte and short widen to int). */
  private static PrimitiveType appendParameter(PrimitiveType type) {
    if (type instanceof PrimitiveType.BooleanType || type instanceof PrimitiveType.CharType) {
      return type;
    }
    if (type instanceof PrimitiveType.IntType) {
      return PrimitiveType.getInt();
    }
    return type; // long, float, double
  }

  /** Stores the built string into {@code result}, or just builds it if {@code result} is null. */
  void finish(@Nullable LValue result) {
    var toString =
        code.instanceCall(builder, builderType, "toString", stringType, List.of(), List.of());
    if (result != null) {
      code.assign(result, toString);
    } else {
      code.invoke(toString);
    }
  }
}
