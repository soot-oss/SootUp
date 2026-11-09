package sootup.core.jimple;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Sahil Agichani
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import sootup.core.util.StringTools;

public class JimpleUtils {

  /**
   * Reserved Jimple grammar keywords matching Soot's Scene.v().reservedNames. When formatting
   * method, field, or class identifiers in Jimple signatures and subsignatures, identifiers
   * matching these names must be enclosed in single quotes (e.g. 'from', 'to') to prevent ambiguous
   * parsing against Jimple keywords.
   */
  private static final Set<String> RESERVED_NAMES =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  "newarray",
                  "newmultiarray",
                  "nop",
                  "ret",
                  "specialinvoke",
                  "staticinvoke",
                  "tableswitch",
                  "virtualinvoke",
                  "null_type",
                  "unknown",
                  "cmp",
                  "cmpg",
                  "cmpl",
                  "entermonitor",
                  "exitmonitor",
                  "interfaceinvoke",
                  "lengthof",
                  "lookupswitch",
                  "neg",
                  "if",
                  "abstract",
                  "annotation",
                  "boolean",
                  "break",
                  "byte",
                  "case",
                  "catch",
                  "char",
                  "class",
                  "final",
                  "native",
                  "public",
                  "protected",
                  "private",
                  "static",
                  "synchronized",
                  "transient",
                  "volatile",
                  "interface",
                  "void",
                  "short",
                  "int",
                  "long",
                  "float",
                  "double",
                  "extends",
                  "implements",
                  "breakpoint",
                  "default",
                  "goto",
                  "instanceof",
                  "new",
                  "return",
                  "throw",
                  "throws",
                  "null",
                  "from",
                  "to")));

  /**
   * Quotes reserved Jimple keywords with single quotes. Method, field, or local names matching
   * reserved Jimple grammar tokens (e.g. 'from', 'to', 'default') must be quoted in signatures and
   * printed Jimple to prevent grammar syntax errors during Jimple parsing.
   */
  public static String quotedNameOf(String s) {
    if (s == null || (s.startsWith("'") && s.endsWith("'") && s.length() >= 2)) {
      return s;
    }
    if (RESERVED_NAMES.contains(s)) {
      return "'" + s + "'";
    }
    return s;
  }

  /** Escapes reserved Jimple keywords e.g. used in (Stmt)Printer, necessary in the JimpleParser */
  public static String escape(String str) {
    if (str.length() == 0) {
      return "\"\"";
    }
    return StringTools.getQuotedStringOf(str, Jimple.jimpleKeywordList().contains(str));
  }

  public static String unescape(String str) {
    StringBuilder sb = new StringBuilder();

    // filter for only \ and not \\ preceeding a possible escapable char
    boolean lastWasRealEscape = false;
    int lastAppendedPos = 0;
    int openHyphenPos = -1;
    for (int i = 0; i < str.length(); i++) {
      if ((str.charAt(i) == '"' || str.charAt(i) == '\'') && !lastWasRealEscape) {
        if (openHyphenPos < 0) {
          if (lastAppendedPos < i) {
            sb.append(StringTools.getUnEscapedStringOf(str.substring(lastAppendedPos, i)));
          }
          openHyphenPos = i;
          lastAppendedPos = i;
        } else if (str.charAt(i) == str.charAt(openHyphenPos)) {
          sb.append(StringTools.getUnEscapedStringOf(str.substring(openHyphenPos + 1, i)));
          openHyphenPos = -1;
          lastAppendedPos = i + 1;
        }
      }
      lastWasRealEscape = !lastWasRealEscape && str.charAt(i) == '\\';
    }

    // if there has been nothing with hyphens etc.
    if (lastAppendedPos < str.length()) {
      sb.append(StringTools.getUnEscapedStringOf(str.substring(lastAppendedPos)));
    }

    return sb.toString();
  }
}
