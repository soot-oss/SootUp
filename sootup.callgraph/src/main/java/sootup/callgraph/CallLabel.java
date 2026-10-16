package sootup.callgraph;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Jonas Klauke
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
 * Marker interface for labels that can be attached to calls (edges) in a call graph. Implement it
 * with an enum or a record to attach custom information to calls, e.g.:
 *
 * <pre>{@code
 * enum Hotness implements CallLabel { HOT, COLD }
 *
 * record TaintFlow(String source, String sink) implements CallLabel {}
 * }</pre>
 *
 * Labels are compared with {@link Object#equals(Object)}, so a label is stored at most once per
 * call.
 */
public interface CallLabel {}
