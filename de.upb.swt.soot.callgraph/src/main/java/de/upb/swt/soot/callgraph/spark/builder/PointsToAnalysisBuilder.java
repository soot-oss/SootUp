package de.upb.swt.soot.callgraph.spark.builder;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2002-2021 Ondrej Lhotak, Kadiray Karakaya and others
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

public class PointsToAnalysisBuilder {

  private ContextSensitivity contextSensitivity = ContextSensitivity.CONTEXT_INSENSITIVE;
  private FieldSensitivity fieldSensitivity = FieldSensitivity.FIELD_INSENSITIVE;
  private boolean ignoreTypes = false;
  private boolean useTypesForSites = false;
  private boolean mergeStringBuffer = true;
  private boolean makeSimpleEdgesBidirectional = false;
  private boolean computeCallGraphOnTheFly = true;

  // TODO: [kk] consider a simplifier interface, to switch between implementations instead of these
  // flags
  private boolean simplifySingleEntrySubgraphs = false;
  private boolean simplifyStronglyConnectedComponents = false;
  private boolean ignoreTypesForStronglyConnectedComponents = false;

  // TODO: [kk] define propagator interface and set concrete propagator

  // VTA: Setting VTA to true has the effect of setting:
  // - field-based,
  // - types-for-sites,
  // - simplify-sccs to true,
  // - on-fly-cg to false, to simulate Variable Type Analysis,

  // RTA: Setting RTA to true sets
  // - types-for-sites to true,
  // - causes Spark to use a single points-to set for all variables

}
