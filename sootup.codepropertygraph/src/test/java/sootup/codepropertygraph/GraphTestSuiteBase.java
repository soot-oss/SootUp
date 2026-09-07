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
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.VoidType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaSootMethod;

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

  protected SootMethod createSootMethod(
      MutableControlFlowGraph controlFlowGraph, String methodName) {
    JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
    MethodSignature methodSignature =
        identifierFactory.getMethodSignature(
            identifierFactory.getClassType("TestClass", "pkg"),
            methodName,
            VoidType.getInstance(),
            Collections.emptyList());

    return new JavaSootMethod(
        new OverridingBodySource(
            methodSignature,
            Body.builder(controlFlowGraph).setMethodSignature(methodSignature).build()),
        methodSignature,
        Collections.singletonList(MethodModifier.PUBLIC),
        Collections.emptyList(),
        NoPositionInformation.getInstance());
  }
}
