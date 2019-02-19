/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 15.11.2018 Markus Schmidt
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

package de.upb.soot.jimple;

import com.ibm.wala.cast.tree.CAstSourcePositionMap;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

public class NoPositionInformation implements CAstSourcePositionMap.Position {

  @Override
  public URL getURL() {
    return null;
  }

  @Override
  public Reader getReader() throws IOException {
    return null;
  }

  @Override
  public int getFirstLine() {
    return 1;
  }

  @Override
  public int getLastLine() {
    return 1;
  }

  @Override
  public int getFirstCol() {
    return 1;
  }

  @Override
  public int getLastCol() {
    return 1;
  }

  @Override
  public int getFirstOffset() {
    return 1;
  }

  @Override
  public int getLastOffset() {
    return 1;
  }

  @Override
  public int compareTo(Object o) {
    return 1;
  }
}
