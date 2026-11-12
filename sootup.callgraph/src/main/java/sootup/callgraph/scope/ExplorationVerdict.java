package sootup.callgraph.scope;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2025 Markus Schmidt
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

/**
 * The verdict returned by {@link VirtualCallResolver#tryAdvanceCall} to control call graph
 * expansion: {@link #EXPLORE_METHOD} admits the edge and expands the callee, {@link
 * #STOP_AFTER_CALL} admits the edge but does not expand the callee via this edge, and {@link #STOP}
 * drops the edge entirely. Which calls of a method are resolved at all is decided earlier by the
 * {@link CallFilter} of a {@link CallResolver}.
 */
public enum ExplorationVerdict {
  EXPLORE_METHOD,
  STOP_AFTER_CALL,
  STOP
}
