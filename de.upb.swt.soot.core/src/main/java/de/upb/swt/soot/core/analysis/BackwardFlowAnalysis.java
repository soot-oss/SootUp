package de.upb.swt.soot.core.analysis;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 1997 - 2021 Raja Vallee-Rai, Kadiray Karakaya and others
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

/**
 * Abstract class that provides the fixed point iteration functionality required by all
 * BackwardFlowAnalyses.
 */
public abstract class BackwardFlowAnalysis<A> extends FlowAnalysis<A> {
  /** Construct the analysis from a DirectedGraph representation of a Body. */
  protected BackwardFlowAnalysis(StmtGraph graph) {
    super(graph);
  }

  /**
   * Returns <code>false</code>
   *
   * @return false
   */
  @Override
  protected boolean isForward() {
    return false;
  }

  @Override
  protected void doAnalysis() {
    super.doAnalysis(GraphView.BACKWARD, stmtToAfterFlow, stmtToBeforeFlow);
  }
}
