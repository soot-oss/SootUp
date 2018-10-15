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

import java.util.EnumSet;

/**
 * An Enum that provides static methods and constants to represent and work with with Java modifiers (ie public, final,...)
 * Represents Java modifiers that can be packed and combined via EnumSet and methods to query
 * these.
 *
 */
public enum Modifier {
  ABSTRACT,
  FINAL,
  INTERFACE,
  NATIVE,
  PRIVATE,
  PROTECTED,
  PUBLIC,
  STATIC,
  SYNCHRONIZED,
  TRANSIENT, /* VARARGS for methods */
  VOLATILE, /* BRIDGE for methods */
  STRICTFP,
  ANNOTATION,
  ENUM,
  MODULE,

  // dex specifific modifiers
  SYNTHETIC,
  CONSTRUCTOR,
  DECLARED_SYNCHRONIZED,

  // modifier for java 9 modules
  OPEN,
  REQUIRES_TRANSITIVE,
  REQUIRES_STATIC,
  REQUIRES_SYNTHETIC,
  REQUIRES_MANDATED;

  public static boolean isAbstract(EnumSet<Modifier> m) {
    return m.contains(ABSTRACT);
  }

  public static boolean isFinal(EnumSet<Modifier> m) {
    return m.contains(FINAL);
  }

  public static boolean isInterface(EnumSet<Modifier> m) {
    return m.contains(INTERFACE);
  }

  public static boolean isNative(EnumSet<Modifier> m) {
    return m.contains(NATIVE);
  }

  public static boolean isPrivate(EnumSet<Modifier> m) {
    return m.contains(PRIVATE);
  }

  public static boolean isProtected(EnumSet<Modifier> m) {
    return m.contains(PROTECTED);
  }

  public static boolean isPublic(EnumSet<Modifier> m) {
    return m.contains(PUBLIC);
  }

  public static boolean isStatic(EnumSet<Modifier> m) {
    return m.contains(STATIC);
  }

  public static boolean isSynchronized(EnumSet<Modifier> m) {
    return m.contains(SYNCHRONIZED);
  }

  public static boolean isTransient(EnumSet<Modifier> m) {
    return m.contains(TRANSIENT);
  }

  public static boolean isVolatile(EnumSet<Modifier> m) {
    return m.contains(VOLATILE);
  }

  public static boolean isStrictFP(EnumSet<Modifier> m) {
    return m.contains(STRICTFP);
  }

  public static boolean isAnnotation(EnumSet<Modifier> m) {
    return m.contains(ANNOTATION);
  }

  public static boolean isEnum(EnumSet<Modifier> m) {
    return m.contains(ENUM);
  }

  public static boolean isSynthetic(EnumSet<Modifier> m) {
    return m.contains(SYNTHETIC);
  }

  public static boolean isConstructor(EnumSet<Modifier> m) {
    return m.contains(CONSTRUCTOR);
  }

  public static boolean isDeclaredSynchronized(EnumSet<Modifier> m) {
    return m.contains(DECLARED_SYNCHRONIZED);
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m
   *          a modifier set
   * @return a textual representation of the modifiers.
   */
  public static String toString(EnumSet<Modifier> m) {
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