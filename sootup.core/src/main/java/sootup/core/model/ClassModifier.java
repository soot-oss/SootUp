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
public enum ClassModifier {
  PUBLIC(0x0001),
  PRIVATE(0x0002),
  PROTECTED(0x0004),

  STATIC(0x0008),
  FINAL(0x0010),

  SUPER(0x0020),
  INTERFACE(0x0200),
  ABSTRACT(0x0400),
  SYNTHETIC(0x1000),
  ANNOTATION(0x2000),
  ENUM(0x4000),

  MODULE(0x8000);

  private final int bytecode;

  ClassModifier(int i) {
    bytecode = i;
  }

  public static boolean isAbstract(@Nonnull Set<ClassModifier> m) {
    return m.contains(ABSTRACT);
  }

  public static boolean isFinal(@Nonnull Set<ClassModifier> m) {
    return m.contains(FINAL);
  }

  public static boolean isInterface(@Nonnull Set<ClassModifier> m) {
    return m.contains(INTERFACE);
  }

  public static boolean isPrivate(@Nonnull Set<ClassModifier> m) {
    return m.contains(PRIVATE);
  }

  public static boolean isProtected(@Nonnull Set<ClassModifier> m) {
    return m.contains(PROTECTED);
  }

  public static boolean isPublic(@Nonnull Set<ClassModifier> m) {
    return m.contains(PUBLIC);
  }

  public static boolean isStatic(@Nonnull Set<ClassModifier> m) {
    return m.contains(STATIC);
  }

  public static boolean isSuper(@Nonnull Set<ClassModifier> m) {
    return m.contains(SUPER);
  }

  public static boolean isAnnotation(@Nonnull Set<ClassModifier> m) {
    return m.contains(ANNOTATION);
  }

  public static boolean isEnum(@Nonnull Set<ClassModifier> m) {
    return m.contains(ENUM);
  }

  public static boolean isSynthetic(@Nonnull Set<ClassModifier> m) {
    return m.contains(SYNTHETIC);
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m a modifier set
   * @return a textual representation of the modifiers.
   */
  @Nonnull
  public static String toString(@Nonnull Set<ClassModifier> m) {
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

    if (isSuper(m)) {
      builder.append("super ");
    }

    if (isAnnotation(m)) {
      builder.append("annotation ");
    }

    if (isEnum(m)) {
      builder.append("enum ");
    }

    if (isInterface(m)) {
      builder.append("interface ");
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
  public static String toString(@Nonnull EnumSet<ClassModifier> m) {
    return m.stream().map((mod) -> mod.name().toLowerCase()).collect(Collectors.joining(" "));
  }

  /** @return the bytecode of this Modifier. */
  public int getBytecode() {
    return bytecode;
  }
}
