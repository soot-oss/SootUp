// Incomplete class

package sootup.core.model;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Jan Martin Persch, Christian Brüggemann and others
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

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;

/**
 * An Enum that provides static methods and constants to represent and work with with Java modifiers
 * (ie public, final,...) Represents Java modifiers that can be packed and combined via EnumSet and
 * methods to query these.
 */
public enum MethodModifier {
  PUBLIC(0x0001),
  PRIVATE(0x0002),
  PROTECTED(0x0004),

  STATIC(0x0008),
  FINAL(0x0010),

  SYNCHRONIZED(0x0020),

  VARARGS(0x0080), /* VARARGS for methods */
  BRIDGE(0x0040),
  NATIVE(0x0100),
  ABSTRACT(0x0400),
  STRICTFP(0x0800),

  // dex specifific modifiers
  SYNTHETIC(0x1000),
  ENUM(0x4000),
  CONSTRUCTOR(0x10000),
  DECLARED_SYNCHRONIZED(0x20000);

  private final int bytecode;

  MethodModifier(int i) {
    bytecode = i;
  }

  public static boolean isAbstract(@Nonnull Set<MethodModifier> m) {
    return m.contains(ABSTRACT);
  }

  public static boolean isFinal(@Nonnull Set<MethodModifier> m) {
    return m.contains(FINAL);
  }

  public static boolean isNative(@Nonnull Set<MethodModifier> m) {
    return m.contains(NATIVE);
  }

  public static boolean isPrivate(@Nonnull Set<MethodModifier> m) {
    return m.contains(PRIVATE);
  }

  public static boolean isProtected(@Nonnull Set<MethodModifier> m) {
    return m.contains(PROTECTED);
  }

  public static boolean isPublic(@Nonnull Set<MethodModifier> m) {
    return m.contains(PUBLIC);
  }

  public static boolean isStatic(@Nonnull Set<MethodModifier> m) {
    return m.contains(STATIC);
  }

  public static boolean isSynchronized(@Nonnull Set<MethodModifier> m) {
    return m.contains(SYNCHRONIZED);
  }

  public static boolean isVarargs(@Nonnull Set<MethodModifier> m) {
    return m.contains(VARARGS);
  }

  public static boolean isBridge(@Nonnull Set<MethodModifier> m) {
    return m.contains(BRIDGE);
  }

  public static boolean isStrictFP(@Nonnull Set<MethodModifier> m) {
    return m.contains(STRICTFP);
  }

  public static boolean isEnum(@Nonnull Set<MethodModifier> m) {
    return m.contains(ENUM);
  }

  public static boolean isSynthetic(@Nonnull Set<MethodModifier> m) {
    return m.contains(SYNTHETIC);
  }

  public static boolean isConstructor(@Nonnull Set<MethodModifier> m) {
    return m.contains(CONSTRUCTOR);
  }

  public static boolean isDeclaredSynchronized(@Nonnull Set<MethodModifier> m) {
    return m.contains(DECLARED_SYNCHRONIZED);
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m a modifier set
   * @return a textual representation of the modifiers.
   */
  @Nonnull
  public static String toString(@Nonnull Set<MethodModifier> m) {
    StringBuilder builder = new StringBuilder();

    if (isPublic(m)) {
      builder.append("public ");
    } else if (isPrivate(m)) {
      builder.append("private ");
    } else if (isProtected(m)) {
      builder.append("protected ");
    }

    if (isAbstract(m)) {
      builder.append("abstract ");
    }

    if (isStatic(m)) {
      builder.append("static ");
    }

    if (isFinal(m)) {
      builder.append("final ");
    }

    if (isSynchronized(m)) {
      builder.append("synchronized ");
    }

    if (isNative(m)) {
      builder.append("native ");
    }

    if (isBridge(m)) {
      builder.append("bridge ");
    }

    if (isVarargs(m)) {
      builder.append("varargs ");
    }

    if (isStrictFP(m)) {
      builder.append("strictfp ");
    }

    if (isEnum(m)) {
      builder.append("enum ");
    }

    // trim
    final int lastCharPos = builder.length() - 1;
    if (lastCharPos > 0) {
      builder.setLength(lastCharPos);
    }
    return builder.toString();
  }

  @Nonnull
  // depends on the natural order of the Enums!
  public static String toString(@Nonnull EnumSet<MethodModifier> m) {
    return m.stream().map((mod) -> mod.name().toLowerCase()).collect(Collectors.joining(" "));
  }

  /** @return the bytecode of this Modifier. */
  public int getBytecode() {
    return bytecode;
  }
}
