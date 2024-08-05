package sootup.codepropertygraph;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import sootup.codepropertygraph.cdg.CdgCreator;
import sootup.codepropertygraph.cfg.CfgCreator;
import sootup.codepropertygraph.propertygraph.PropertyGraph;
import sootup.codepropertygraph.propertygraph.edges.PropertyGraphEdge;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.types.VoidType;
import sootup.java.core.types.JavaClassType;

public abstract class GraphTestSuiteBase {

  protected CfgCreator cfgCreator;
  protected CdgCreator cdgCreator;

  @BeforeEach
  public void setUp() {
    cfgCreator = new CfgCreator();
    cdgCreator = new CdgCreator();
  }

  protected void verifyEdges(
      PropertyGraph graph, Class<? extends PropertyGraphEdge>... expectedEdgeTypes) {
    assertNotNull(graph);

    if (graph.getEdges().size() == 0) return;
    for (Class<? extends PropertyGraphEdge> edgeType : expectedEdgeTypes) {
      assertTrue(
          graph.getEdges().stream().anyMatch(edgeType::isInstance),
          "Expected edge type not found: " + edgeType.getSimpleName());
    }
  }

  protected SootMethod createSootMethod(MutableStmtGraph stmtGraph, String methodName) {
    MethodSignature methodSignature =
        new MethodSignature(
            new JavaClassType("TestClass", new PackageName("pkg")),
            methodName,
            Collections.emptyList(),
            VoidType.getInstance());

    return new SootMethod(
        new OverridingBodySource(
            methodSignature, Body.builder(stmtGraph).setMethodSignature(methodSignature).build()),
        methodSignature,
        Collections.singletonList(MethodModifier.PUBLIC),
        Collections.emptyList(),
        NoPositionInformation.getInstance());
  }
}
