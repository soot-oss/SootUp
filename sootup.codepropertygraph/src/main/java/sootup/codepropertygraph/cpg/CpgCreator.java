package sootup.codepropertygraph.cpg;

import sootup.codepropertygraph.ast.AstCreator;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.ddg.DdgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.utils.PropertyGraphsMerger;
import sootup.core.model.SootMethod;
import sootup.codepropertygraph.cfg.CfgCreator;

public class CpgCreator {
  private final AstCreator astCreator;
  private final CfgCreator cfgCreator;
  private final CdgCreator cdgCreator;
  private final DdgCreator ddgCreator;

  public CpgCreator(
      AstCreator astCreator, CfgCreator cfgCreator, CdgCreator cdgCreator, DdgCreator ddgCreator) {
    this.astCreator = astCreator;
    this.cfgCreator = cfgCreator;
    this.cdgCreator = cdgCreator;
    this.ddgCreator = ddgCreator;
  }

  public PropertyGraph createCpg(SootMethod method) {
    PropertyGraph cpgGraph = astCreator.createGraph(method);
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, cfgCreator.createGraph(method));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, cdgCreator.createGraph(method));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, ddgCreator.createGraph(method));
    return cpgGraph;
  }
}
