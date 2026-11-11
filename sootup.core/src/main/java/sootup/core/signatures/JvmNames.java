package sootup.core.signatures;

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

import org.jspecify.annotations.NonNull;

/**
 * Unqualified-name rules of JVMS 4.2.2, checked when a signature is interned so no signature with
 * an illegal name exists. Names are stored unescaped; Jimple quoting is a printer/parser concern.
 */
public final class JvmNames {

  private JvmNames() {}

  /**
   * Field name: non-empty, none of {@code . ; [ /}.
   *
   * @throws IllegalArgumentException otherwise
   */
  public static void checkFieldName(@NonNull String name) {
    checkUnqualified(name, "field");
  }

  /**
   * Method name: field-name rules, plus {@code < >} only as {@code <init>} or {@code <clinit>}.
   *
   * @throws IllegalArgumentException otherwise
   */
  public static void checkMethodName(@NonNull String name) {
    if (name.equals("<init>") || name.equals("<clinit>")) {
      return;
    }
    checkUnqualified(name, "method");
    if (name.indexOf('<') >= 0 || name.indexOf('>') >= 0) {
      throw new IllegalArgumentException(
          "Illegal method name '" + name + "': '<' and '>' only in <init>, <clinit>");
    }
  }

  private static void checkUnqualified(String name, String kind) {
    if (name.isEmpty()) {
      throw new IllegalArgumentException("Empty " + kind + " name");
    }
    for (int i = 0; i < name.length(); i++) {
      char c = name.charAt(i);
      if (c == '.' || c == ';' || c == '[' || c == '/') {
        throw new IllegalArgumentException(
            "Illegal " + kind + " name '" + name + "': contains '" + c + "' (JVMS 4.2.2)");
      }
    }
  }
}
