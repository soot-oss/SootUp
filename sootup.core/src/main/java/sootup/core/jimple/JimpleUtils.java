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

import sootup.core.util.StringTools;

public class JimpleUtils {
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
