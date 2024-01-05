package sootup.java.codepropertygraph.cpg;

import sootup.java.codepropertygraph.MethodInfo;
import sootup.java.codepropertygraph.ast.AstCreator;
import sootup.java.codepropertygraph.cdg.CdgCreator;
import sootup.java.codepropertygraph.cfg.CfgCreator;
import sootup.java.codepropertygraph.ddg.DdgCreator;
import sootup.java.codepropertygraph.propertygraph.PropertyGraph;
import sootup.java.codepropertygraph.propertygraph.PropertyGraphsMerger;

public class CpgCreator {
  public static PropertyGraph convert(MethodInfo methodInfo) {
    PropertyGraph cpgGraph = AstCreator.convert(methodInfo);
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, CfgCreator.convert(methodInfo));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, CdgCreator.convert(methodInfo));
    cpgGraph = PropertyGraphsMerger.mergeGraphs(cpgGraph, DdgCreator.convert(methodInfo));

    return cpgGraph;
  }
}
