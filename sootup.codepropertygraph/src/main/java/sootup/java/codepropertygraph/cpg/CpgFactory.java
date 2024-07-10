package sootup.java.codepropertygraph.cpg;

import sootup.core.model.SootMethod;
import sootup.java.codepropertygraph.ast.AstCreator;
import sootup.java.codepropertygraph.cdg.CdgCreator;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.ddg.DdgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphsMerger;

public class CpgFactory {
  private final AstCreator astCreator;
  private final CfgCreator cfgCreator;
  private final CdgCreator cdgCreator;
  private final DdgCreator ddgCreator;

  public CpgFactory(
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
