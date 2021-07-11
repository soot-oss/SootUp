package de.upb.swt.soot.core.analysis;

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


import de.upb.swt.soot.core.graph.StmtGraph;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;

import java.util.List;

/**
 * Provides an interface for querying for the definitions of a Local at a given stmt in a method.
 */
public interface LocalDefs {

    /**
     * Returns the definition sites for a Local at a certain point (stmt) in a method.
     *
     * You can assume this method never returns {@code null}.
     *
     * @param l
     *          the Local in question.
     * @param s
     *          a stmt that specifies the method context (location) to query for the definitions of the Local.
     * @return a list of stmts where the local is defined in the current method context. If there are no uses an empty list
     *         will returned.
     */
    public List<Stmt> getDefsOfAt(Local l, Stmt s);

    /**
     * Returns the definition sites for a Local merged over all points in a method.
     *
     * You can assume this method never returns {@code null}.
     *
     * @param l
     *          the Local in question.
     * @return a list of Stmts where the local is defined in the current method context. If there are no uses an empty list
     *         will returned.
     */
    public List<Stmt> getDefsOf(Local l);

    /**
     *
     */
    public static final class Factory {
        private Factory() {
        }

        /**
         * Creates a new LocalDefs analysis based on a {@code ExceptionalStmtGraph}
         *
         * @see de.upb.swt.soot.core.graph.ExceptionalStmtGraph
         * @param body
         * @return a new LocalDefs instance
         */
        public static LocalDefs newLocalDefs(Body body) {
            return newLocalDefs(body, false);
        }


        /**
         * Creates a new LocalDefs analysis based on a given {@code StmtGraph}. If you don't trust the input you should set
         * {@code expectUndefined} to {@code true}.
         *
         * @see Body
         * @param body
         *          the body to work with
         * @param expectUndefined
         *          if you expect uses of locals that are undefined
         * @return a new LocalDefs instance
         */
        public static LocalDefs newLocalDefs(Body body, boolean expectUndefined) {
            // return new SmartLocalDefs(graph, LiveLocals.Factory.newLiveLocals(graph));
            return new SimpleLocalDefs(body, expectUndefined ? FlowAnalysisMode.OmitSSA : FlowAnalysisMode.Automatic);
        }

        /**
         * Creates a new LocalDefs analysis based on a given {@code StmtGraph}. This analysis will be flow-insensitive, i.e., for
         * a given local, it will always give all statements that ever write to that local regardless of potential redefinitions
         * in between.
         *
         * @see StmtGraph
         * @param body
         *          the body to work with
         * @return a new LocalDefs instance
         */
        public static LocalDefs newLocalDefsFlowInsensitive(Body body) {
            // return new SmartLocalDefs(graph, LiveLocals.Factory.newLiveLocals(graph));
            return new SimpleLocalDefs(body, FlowAnalysisMode.FlowInsensitive);
        }
    }

    /**
     * The different modes in which the flow analysis can run
     */
    enum FlowAnalysisMode {
        /**
         * Automatically detect the mode to use
         */
        Automatic,
        /**
         * Never use the SSA form, even if the stmt graph would allow for a flow-insensitive analysis without losing precision
         */
        OmitSSA,
        /**
         * Always conduct a flow-insensitive analysis
         */
        FlowInsensitive
    }
}
