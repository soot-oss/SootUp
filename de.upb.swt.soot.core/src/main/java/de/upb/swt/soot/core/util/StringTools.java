package de.upb.swt.soot.core.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 1999 Raja Vallee-Rai
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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

/** Utility methods for string manipulations commonly used in Soot. */
public class StringTools {

  /**
   * Returns fromString, but with non-isalpha() characters printed as <code>'\\unnnn'</code>. Used
   * by SootClass to generate output.
   */
  public static java.lang.String getEscapedStringOf(String fromString) {
    char[] fromStringArray;
    int cr;
    int lf;
    int ch;
    StringBuilder whole = new StringBuilder();
    StringBuilder mini = new StringBuilder();

    fromStringArray = fromString.toCharArray();

    cr = lineSeparator.charAt(0);
    lf = -1;

    if (lineSeparator.length() == 2) {
      lf = lineSeparator.charAt(1);
    }

    for (char element : fromStringArray) {
      ch = element;
      if (((ch >= 32 && ch <= 126) || ch == cr || ch == lf) && ch != '\\') {
        whole.append((char) ch);

        continue;
      }

      mini.setLength(0);
      mini.append(Integer.toHexString(ch));

      while (mini.length() < 4) {
        mini.insert(0, "0");
      }

      mini.insert(0, "\\u");
      whole.append(mini.toString());
    }

    return whole.toString();
  }

  /** Convenience field storing the system line separator. */
  public static final String lineSeparator = System.getProperty("line.separator");

  /**
   * Returns fromString, but with certain characters printed as if they were in a Java string
   * literal. Used by StringConstant.toString()
   */
  public static String getQuotedStringOf(String fromString) {
    // We definitely need fromString.length + 2, but let's have some
    // additional space
    StringBuilder builder = new StringBuilder(fromString.length() + 20);
    builder.append("\"");
    for (int i = 0; i < fromString.length(); i++) {
      char ch = fromString.charAt(i);
      if (ch == '\\') {
        builder.append("\\\\");
      } else if (ch == '\'') {
        builder.append("\\\'");
      } else if (ch == '\"') {
        builder.append("\\\"");
      } else if (ch == '\n') {
        builder.append("\\n");
      } else if (ch == '\t') {
        builder.append("\\t");
      }
      /*
       * 04.04.2006 mbatch added handling of \r, as compilers throw error if unicode
       */
      else if (ch == '\r') {
        builder.append("\\r");
      }
      /*
       * 10.04.2006 Nomait A Naeem added handling of \f, as compilers throw error if unicode
       */
      else if (ch == '\f') {
        builder.append("\\f");
      } else if (ch >= 32 && ch <= 126) {
        builder.append(ch);
      } else {
        builder.append(getUnicodeStringFromChar(ch));
      }
    }

    builder.append("\"");
    return builder.toString();
  }

  /**
   * Returns a String containing the escaped <code>\\unnnn</code> representation for <code>ch</code>
   * .
   */
  public static String getUnicodeStringFromChar(char ch) {
    String s = Integer.toHexString(ch);
    String padding = null;

    switch (s.length()) {
      case 1:
        padding = "000";
        break;
      case 2:
        padding = "00";
        break;
      case 3:
        padding = "0";
        break;
      case 4:
        padding = "";
        break;
    }

    return "\\u" + padding + s;
  }

  /**
   * Returns a String de-escaping the <code>\\unnnn</code> representation for any escaped characters
   * in the string.
   */
  public static String getUnEscapedStringOf(String str) {
    StringBuilder buf = new StringBuilder();
    CharacterIterator iter = new StringCharacterIterator(str);

    for (char ch = iter.first(); ch != CharacterIterator.DONE; ch = iter.next()) {
      if (ch != '\\') {
        buf.append(ch);
      } else { // enter escaped mode
        ch = iter.next();
        char format;

        if (ch == '\\') {
          buf.append(ch);
        } else if ((format = getCFormatChar(ch)) != '\0') {
          buf.append(format);
        } else if (ch == 'u') { // enter unicode mode
          StringBuilder mini = new StringBuilder(4);
          for (int i = 0; i < 4; i++) {
            mini.append(iter.next());
          }

          ch = (char) Integer.parseInt(mini.toString(), 16);
          buf.append(ch);
        } else {
          throw new RuntimeException("Unexpected char: " + ch);
        }
      }
    }
    return buf.toString();
  }

  /** Returns the canonical C-string representation of c. */
  public static char getCFormatChar(char c) {
    char res;

    switch (c) {
      case 'n':
        res = '\n';
        break;
      case 't':
        res = '\t';
        break;
      case 'r':
        res = '\r';
        break;
      case 'b':
        res = '\b';
        break;
      case 'f':
        res = '\f';
        break;
      case '\"':
        res = '\"';
        break;
      case '\'':
        res = '\'';
        break;

      default:
        res = '\0';
        break;
    }
    return res;
  }
}
