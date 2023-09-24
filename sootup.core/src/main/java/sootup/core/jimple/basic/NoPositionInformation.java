package sootup.core.jimple.basic;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2018-2020 Linghui Luo, Markus Schmidt
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

import sootup.core.model.FullPosition;

/**
 * This convenience class represents the case when there is no position information available.
 *
 * @author Linghui Luo
 */
public class NoPositionInformation extends FullPosition {

  private static final NoPositionInformation INSTANCE = new NoPositionInformation();

  private NoPositionInformation() {
    super(-1, -1, -1, -1);
  }

  public static NoPositionInformation getInstance() {
    return INSTANCE;
  }

  @Override
  public String toString() {
    return "NoPositionInformation";
  }
}
