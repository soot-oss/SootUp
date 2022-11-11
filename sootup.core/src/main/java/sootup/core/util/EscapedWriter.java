package sootup.core.util;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997-2020 Raja Vallee-Rai, Christian Brüggemann, Markus Schmidt
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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * A FilterWriter which catches to-be-escaped characters (<code>\\unnnn</code>) in the input and
 * substitutes their escaped representation. Used for Soot output.
 */
public class EscapedWriter extends FilterWriter {
  /** Convenience field containing the system's line separator. */
  private final String lineSeparator = System.getProperty("line.separator");

  private final int cr = lineSeparator.charAt(0);
  private final int lf = (lineSeparator.length() == 2) ? lineSeparator.charAt(1) : -1;

  /** Constructs an EscapedWriter around the given Writer. */
  public EscapedWriter(Writer fos) {
    super(fos);
  }

  /** Print a single character (unsupported). */
  public void print(int ch) throws IOException {
    write(ch);
    throw new RuntimeException();
  }

  /** Write a segment of the given String. */
  public void write(String s, int off, int len) throws IOException {
    final int end = off + len;
    for (int i = off; i < end; i++) {
      write(s.charAt(i));
    }
  }

  private final StringBuilder mini = new StringBuilder(8);

  private boolean isClean(int ch) {
    return ch >= 32 && ch <= 126 || ch == cr || ch == lf;
  }

  /** Write a single character. */
  public void write(int ch) throws IOException {
    if (isClean(ch)) {
      super.write(ch);
      return;
    }

    mini.setLength(0);
    mini.append(Integer.toHexString(ch));
    final int len = mini.length();

    // prepend \\u + [0]{0,4} to hex string so it writes "\\u" + "minimum 4 alphanumeric chars and
    // max. 8"
    int cutPos = (len < 4) ? 2 + 4 - len : 2;
    super.write("\\u0000", 0, cutPos);
    super.write(mini.toString(), 0, len);
  }
}
