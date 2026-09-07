package qilin.pta.toolkits.moon.graph;

/*-
 * #%L
 * SootUp - a J*va Optimization Framework
 * %%
 * Copyright (C) 2026 Markus Schmidt and others
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

public enum FlowKind {
  NEW,
  LOCAL_ASSIGN,
  INSTANCE_LOAD,
  INSTANCE_STORE,
  STATIC_LOAD,
  STATIC_STORE,
  THIS_PASSING,

  PARAMETER_PASSING,
  RETURN,
  FIELD_STORE,
  FIELD_LOAD,
  CALL_STORE,
  CALL_LOAD,
  EXCEPTION_FLOW,
  IGNORE,

  THIS_PARAM_PASSING, // this is for the case that the method is called by another method in the
  // same class

  THIS_CONNECT, // for inlinePtrStreamGraph

  // only for StoredVarTraverser.java
  STATIC_METHOD_RETURN,
  STATIC_PARAMETER_PASSING,
  THIS_METHOD_RETURN,
  THIS_FIELD_STORE_AND_LOAD
}
