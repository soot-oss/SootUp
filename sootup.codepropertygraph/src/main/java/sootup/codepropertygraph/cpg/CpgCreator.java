package sootup.codepropertygraph.cpg;

/*-
* #%L
* Soot - a J*va Optimization Framework
* %%
Copyright (C) 2024 Michael Youkeim, Stefan Schott and others
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

import sootup.codepropertygraph.ast.AstCreator;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.cfg.CfgCreator;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.util.PropertyGraphsMerger;
import sootup.core.model.SootMethod;

/** This class is responsible for creating the Code Property Graph (CPG) for a given Soot method. */
public class CpgCreator {
  private final AstCreator astCreator;
  private final CfgCreator cfgCreator;
  private final CdgCreator cdgCreator;
  private final DdgCreator ddgCreator;

  /**
   * Constructs a CPG creator with the specified creators for AST, CFG, CDG, and DDG.
   *
   * @param astCreator the AST creator
   * @param cfgCreator the CFG creator
   * @param cdgCreator the CDG creator
   * @param ddgCreator the DDG creator
   */
  public CpgCreator(
      AstCreator astCreator, CfgCreator cfgCreator, CdgCreator cdgCreator, DdgCreator ddgCreator) {
    this.astCreator = astCreator;
    this.cfgCreator = cfgCreator;
    this.cdgCreator = cdgCreator;
    this.ddgCreator = ddgCreator;
  }

  /**
   * Creates the CPG for the given Soot method.
   *
   * @param method the Soot method
   * @return the CPG
   */
  public PropertyGraph createCpg(SootMethod method) {
    PropertyGraph cpgGraph = astCreator.createGraph(method);
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, cfgCreator.createGraph(method));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, cdgCreator.createGraph(method));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, ddgCreator.createGraph(method));
    return cpgGraph;
  }
}
