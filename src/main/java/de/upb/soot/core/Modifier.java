/* Soot - a J*va Optimization Framework
 * Copyright (C) 1997-1999 Raja Vallee-Rai
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

/*
 * Modified by the Sable Research Group and others 1997-1999.
 * See the 'credits' file distributed with Soot for the complete list of
 * contributors.  (Soot is distributed at http://www.sable.mcgill.ca/soot)
 */

// Incomplete class

package de.upb.soot.core;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.Set;

/**
 * An Enum that provides static methods and constants to represent and work with with Java modifiers
 * (ie public, final,...) Represents Java modifiers that can be packed and combined via EnumSet and
 * methods to query these.
 */
public enum Modifier {
  ABSTRACT(0x0400),
  FINAL(0x0010),
  INTERFACE(0x0200),
  NATIVE(0x0100),
  PRIVATE(0x0002),
  PROTECTED(0x0004),
  PUBLIC(0x0001),
  STATIC(0x0008),
  SYNCHRONIZED(0x0020),
  TRANSIENT(0x0080), /* VARARGS for methods */
  VOLATILE(0x0040), /* BRIDGE for methods */
  STRICTFP(0x0800),
  ANNOTATION(0x2000),
  ENUM(0x4000),
  MODULE(0x8000),

  // dex specifific modifiers
  SYNTHETIC(0x1000),
  CONSTRUCTOR(0x10000),
  DECLARED_SYNCHRONIZED(0x20000),

  // modifier for java 9 modules
  OPEN(0x0020),
  REQUIRES_TRANSITIVE(0x0020),
  REQUIRES_STATIC(0x0040),
  REQUIRES_SYNTHETIC(0x1000),
  REQUIRES_MANDATED(0x8000);

  private final int bytecode;

  Modifier(int i) {
    this.bytecode = i;
  }

  public static boolean isAbstract(@Nonnull Set<Modifier> m) {
    return m.contains(ABSTRACT);
  }

  public static boolean isFinal(@Nonnull Set<Modifier> m) {
    return m.contains(FINAL);
  }

  public static boolean isInterface(@Nonnull Set<Modifier> m) {
    return m.contains(INTERFACE);
  }

  public static boolean isNative(@Nonnull Set<Modifier> m) {
    return m.contains(NATIVE);
  }

  public static boolean isPrivate(@Nonnull Set<Modifier> m) {
    return m.contains(PRIVATE);
  }

  public static boolean isProtected(@Nonnull Set<Modifier> m) {
    return m.contains(PROTECTED);
  }

  public static boolean isPublic(@Nonnull Set<Modifier> m) {
    return m.contains(PUBLIC);
  }

  public static boolean isStatic(@Nonnull Set<Modifier> m) {
    return m.contains(STATIC);
  }

  public static boolean isSynchronized(@Nonnull Set<Modifier> m) {
    return m.contains(SYNCHRONIZED);
  }

  public static boolean isTransient(@Nonnull Set<Modifier> m) {
    return m.contains(TRANSIENT);
  }

  public static boolean isVolatile(@Nonnull Set<Modifier> m) {
    return m.contains(VOLATILE);
  }

  public static boolean isStrictFP(@Nonnull Set<Modifier> m) {
    return m.contains(STRICTFP);
  }

  public static boolean isAnnotation(@Nonnull Set<Modifier> m) {
    return m.contains(ANNOTATION);
  }

  public static boolean isEnum(@Nonnull Set<Modifier> m) {
    return m.contains(ENUM);
  }

  public static boolean isSynthetic(@Nonnull Set<Modifier> m) {
    return m.contains(SYNTHETIC);
  }

  public static boolean isConstructor(@Nonnull Set<Modifier> m) {
    return m.contains(CONSTRUCTOR);
  }

  public static boolean isDeclaredSynchronized(@Nonnull Set<Modifier> m) {
    return m.contains(DECLARED_SYNCHRONIZED);
  }

  public static boolean isRequiresTransitive(EnumSet<Modifier> modifiers) {
    return modifiers.contains(REQUIRES_TRANSITIVE);
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m a modifier set
   * @return a textual representation of the modifiers.
   */
  @Nonnull
  public static String toString(@Nonnull Set<Modifier> m) {
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

    if (isTransient(m)) {
      builder.append("transient ");
    }

    if (isVolatile(m)) {
      builder.append("volatile ");
    }

    if (isStrictFP(m)) {
      builder.append("strictfp ");
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

    return (builder.toString()).trim();
  }

  /** @return the bytecode of this Modifier. */
  public int getBytecode() {
    return this.bytecode;
  }
}
