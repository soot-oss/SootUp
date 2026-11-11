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
import org.jspecify.annotations.NonNull;

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

  public static boolean isAbstract(@NonNull Set<ClassModifier> m) {
    return m.contains(ABSTRACT);
  }

  public static boolean isFinal(@NonNull Set<ClassModifier> m) {
    return m.contains(FINAL);
  }

  public static boolean isInterface(@NonNull Set<ClassModifier> m) {
    return m.contains(INTERFACE);
  }

  public static boolean isPrivate(@NonNull Set<ClassModifier> m) {
    return m.contains(PRIVATE);
  }

  public static boolean isProtected(@NonNull Set<ClassModifier> m) {
    return m.contains(PROTECTED);
  }

  public static boolean isPublic(@NonNull Set<ClassModifier> m) {
    return m.contains(PUBLIC);
  }

  public static boolean isStatic(@NonNull Set<ClassModifier> m) {
    return m.contains(STATIC);
  }

  public static boolean isSuper(@NonNull Set<ClassModifier> m) {
    return m.contains(SUPER);
  }

  public static boolean isAnnotation(@NonNull Set<ClassModifier> m) {
    return m.contains(ANNOTATION);
  }

  public static boolean isEnum(@NonNull Set<ClassModifier> m) {
    return m.contains(ENUM);
  }

  public static boolean isSynthetic(@NonNull Set<ClassModifier> m) {
    return m.contains(SYNTHETIC);
  }

  /**
   * Rejects combinations no valid class file can hold (JVMS 4.1): more than one access flag, final
   * together with abstract, a final or enum interface, or an annotation that is no interface.
   *
   * @throws IllegalArgumentException on an illegal combination
   */
  public static void checkWellFormed(@NonNull Set<ClassModifier> m) {
    if ((isPublic(m) ? 1 : 0) + (isPrivate(m) ? 1 : 0) + (isProtected(m) ? 1 : 0) > 1) {
      throw new IllegalArgumentException(
          "Class may have at most one of public, protected, private: " + m);
    }
    if (isFinal(m) && isAbstract(m)) {
      throw new IllegalArgumentException("Class may not be both final and abstract: " + m);
    }
    if (isInterface(m) && (isFinal(m) || isEnum(m))) {
      throw new IllegalArgumentException("Interface may not be final or enum: " + m);
    }
    if (isAnnotation(m) && !isInterface(m)) {
      throw new IllegalArgumentException("Annotation must be an interface: " + m);
    }
  }

  /**
   * Converts the given modifiers to their string representation, in canonical form.
   *
   * @param m a modifier set
   * @return a textual representation of the modifiers.
   */
  @NonNull
  public static String toString(@NonNull Set<ClassModifier> m) {
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

  @NonNull
  // depends on the natural order of the Enums!
  public static String toString(@NonNull EnumSet<ClassModifier> m) {
    return m.stream().map((mod) -> mod.name().toLowerCase()).collect(Collectors.joining(" "));
  }

  /**
   * @return the bytecode of this Modifier.
   */
  public int getBytecode() {
    return bytecode;
  }
}
