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

/**
 * An Enum that provides static methods and constants to represent and work with with Java modifiers (ie public, final,...)
 * Represents Java modifiers as int constants that can be packed and combined by bitwise operations and methods to query
 * these.
 *
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

  private final int value;

  Modifier(int i) {
    this.value = i;
  }

  public static boolean isAbstract(int m) {
    return (m & ABSTRACT.value) != 0;
  }

  public static boolean isFinal(int m) {
    return (m & FINAL.value) != 0;
  }

  public static boolean isInterface(int m) {
    return (m & INTERFACE.value) != 0;
  }

  public static boolean isNative(int m) {
    return (m & NATIVE.value) != 0;
  }

  public static boolean isPrivate(int m) {
    return (m & PRIVATE.value) != 0;
  }

  public static boolean isProtected(int m) {
    return (m & PROTECTED.value) != 0;
  }

  public static boolean isPublic(int m) {
    return (m & PUBLIC.value) != 0;
  }

  public static boolean isStatic(int m) {
    return (m & STATIC.value) != 0;
  }

  public static boolean isSynchronized(int m) {
    return (m & SYNCHRONIZED.value) != 0;
  }

  public static boolean isTransient(int m) {
    return (m & TRANSIENT.value) != 0;
  }

  public static boolean isVolatile(int m) {
    return (m & VOLATILE.value) != 0;
  }

  public static boolean isStrictFP(int m) {
    return (m & STRICTFP.value) != 0;
  }

  public static boolean isAnnotation(int m) {
    return (m & ANNOTATION.value) != 0;
  }

  public static boolean isEnum(int m) {
    return (m & ENUM.value) != 0;
  }

  public static boolean isSynthetic(int m) {
    return (m & SYNTHETIC.value) != 0;
  }

  public static boolean isConstructor(int m) {
    return (m & CONSTRUCTOR.value) != 0;
  }

  public static boolean isDeclaredSynchronized(int m) {
    return (m & DECLARED_SYNCHRONIZED.value) != 0;
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m
   *          a modifier set
   * @return a textual representation of the modifiers.
   */
  public static String toString(int m) {
    StringBuffer buffer = new StringBuffer();

    if (isPublic(m)) {
      buffer.append("public ");
    } else if (isPrivate(m)) {
      buffer.append("private ");
    } else if (isProtected(m)) {
      buffer.append("protected ");
    }

    if (isAbstract(m)) {
      buffer.append("abstract ");
    }

    if (isStatic(m)) {
      buffer.append("static ");
    }

    if (isFinal(m)) {
      buffer.append("final ");
    }

    if (isSynchronized(m)) {
      buffer.append("synchronized ");
    }

    if (isNative(m)) {
      buffer.append("native ");
    }

    if (isTransient(m)) {
      buffer.append("transient ");
    }

    if (isVolatile(m)) {
      buffer.append("volatile ");
    }

    if (isStrictFP(m)) {
      buffer.append("strictfp ");
    }

    if (isAnnotation(m)) {
      buffer.append("annotation ");
    }

    if (isEnum(m)) {
      buffer.append("enum ");
    }

    if (isInterface(m)) {
      buffer.append("interface ");
    }

    return (buffer.toString()).trim();
  }

}