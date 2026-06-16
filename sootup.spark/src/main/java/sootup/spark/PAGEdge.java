package sootup.spark;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2002-2025 Ondrej Lhotak, Kadiray Karakaya and others
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

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class PAGEdge {

  public enum EdgeType {
    ALLOCATION,
    ASSIGNMENT,
    STORE,
    LOAD
  }

  private final EdgeType edgeType;

  public static PAGEdge allocation() {
    return new PAGEdge(EdgeType.ALLOCATION);
  }

  public static PAGEdge assignment() {
    return new PAGEdge(EdgeType.ASSIGNMENT);
  }

  public static PAGEdge store() {
    return new PAGEdge(EdgeType.STORE);
  }

  public static PAGEdge load() {
    return new PAGEdge(EdgeType.LOAD);
  }
}
