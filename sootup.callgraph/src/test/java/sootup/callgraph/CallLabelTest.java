package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.callgraph.CallGraph.Call;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

public class CallLabelTest {

  enum Hotness implements CallLabel {
    HOT,
    COLD
  }

  record TaintFlow(String source, String sink) implements CallLabel {}

  private MutableCallGraph cg;
  private MethodSignature mainMethod;
  private MethodSignature staticDispatchB;
  private MethodSignature virtualDispatchB;
  private Call staticCall;
  private Call virtualCall;

  @BeforeEach
  public void setUp() {
    JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                "src/test/resources/callgraph/CallGraphDifference/binary/",
                SourceType.Application,
                Collections.emptyList()));
    JavaIdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaClassType exampleClass = identifierFactory.getClassType("Example");
    mainMethod =
        identifierFactory.getMethodSignature(exampleClass, identifierFactory.getMainSubSignature());
    staticDispatchB =
        identifierFactory.getMethodSignature(
            "B", "staticDispatch", "void", List.of("java.lang.Object"));
    virtualDispatchB =
        identifierFactory.getMethodSignature("B", "virtualDispatch", "void", List.of());

    CallGraph callGraph =
        new ClassHierarchyAnalysisAlgorithm(view).initialize(Collections.singletonList(mainMethod));
    assertInstanceOf(MutableCallGraph.class, callGraph);
    cg = (MutableCallGraph) callGraph;

    staticCall = callFromMainTo(staticDispatchB);
    virtualCall = callFromMainTo(virtualDispatchB);
  }

  private Call callFromMainTo(MethodSignature target) {
    return cg.callsFrom(mainMethod).stream()
        .filter(call -> call.targetMethodSignature().equals(target))
        .findFirst()
        .orElseThrow();
  }

  @Test
  public void testUnlabeledCallHasNoLabels() {
    assertTrue(cg.getLabels(staticCall).isEmpty());
    assertFalse(cg.hasLabel(staticCall, Hotness.HOT));
  }

  @Test
  public void testAddLabel() {
    cg.addLabel(staticCall, Hotness.HOT);

    assertEquals(Set.of(Hotness.HOT), cg.getLabels(staticCall));
    assertTrue(cg.hasLabel(staticCall, Hotness.HOT));
    assertFalse(cg.hasLabel(staticCall, Hotness.COLD));
    assertTrue(cg.getLabels(virtualCall).isEmpty());
  }

  @Test
  public void testLabelsAreNotDuplicated() {
    cg.addLabel(staticCall, Hotness.HOT);
    cg.addLabel(staticCall, Hotness.HOT);
    cg.addLabel(staticCall, new TaintFlow("a", "b"));
    cg.addLabel(staticCall, new TaintFlow("a", "b"));

    assertEquals(2, cg.getLabels(staticCall).size());
  }

  @Test
  public void testMultipleLabelsKeepInsertionOrder() {
    TaintFlow flow = new TaintFlow("source", "sink");
    cg.addLabel(staticCall, flow);
    cg.addLabel(staticCall, Hotness.COLD);

    assertEquals(List.of(flow, Hotness.COLD), List.copyOf(cg.getLabels(staticCall)));
    assertTrue(
        cg.getLabels(staticCall).stream()
            .anyMatch(label -> label instanceof TaintFlow t && t.sink().equals("sink")));
  }

  @Test
  public void testLabelsAreUnmodifiable() {
    cg.addLabel(staticCall, Hotness.HOT);
    Set<CallLabel> labels = cg.getLabels(staticCall);
    assertThrows(UnsupportedOperationException.class, () -> labels.add(Hotness.COLD));
    assertThrows(
        UnsupportedOperationException.class, () -> cg.getLabels(virtualCall).add(Hotness.COLD));
  }

  @Test
  public void testAddLabelToMissingCallThrows() {
    Call missingCall =
        new Call(
            staticCall.targetMethodSignature(),
            staticCall.sourceMethodSignature(),
            staticCall.invokableStmt());
    assertFalse(cg.containsCall(missingCall));
    assertThrows(IllegalArgumentException.class, () -> cg.addLabel(missingCall, Hotness.HOT));
  }

  @Test
  public void testAddCallWithLabel() {
    int callCount = cg.callCount();
    // existing call: only the label is added
    cg.addCall(staticCall, Hotness.HOT);
    assertEquals(callCount, cg.callCount());
    assertTrue(cg.hasLabel(staticCall, Hotness.HOT));

    // new call: call and label are added
    Call newCall =
        new Call(
            staticCall.targetMethodSignature(),
            staticCall.sourceMethodSignature(),
            staticCall.invokableStmt());
    cg.addCall(newCall, Hotness.COLD);
    assertEquals(callCount + 1, cg.callCount());
    assertTrue(cg.containsCall(newCall));
    assertEquals(Set.of(Hotness.COLD), cg.getLabels(newCall));
  }

  @Test
  public void testFilterCallsByLabel() {
    cg.addLabel(staticCall, Hotness.HOT);
    cg.addLabel(virtualCall, Hotness.COLD);

    assertEquals(Set.of(staticCall), cg.callsFrom(mainMethod, label -> label == Hotness.HOT));
    assertEquals(Set.of(virtualCall), cg.callsFrom(mainMethod, label -> label == Hotness.COLD));
    assertEquals(
        Set.of(staticCall, virtualCall),
        cg.callsFrom(mainMethod, label -> label instanceof Hotness));
    assertTrue(cg.callsFrom(mainMethod, label -> label instanceof TaintFlow).isEmpty());

    assertEquals(Set.of(staticCall), cg.callsTo(staticDispatchB, label -> label == Hotness.HOT));
    assertTrue(cg.callsTo(virtualDispatchB, label -> label == Hotness.HOT).isEmpty());
  }

  @Test
  public void testCopyContainsIndependentLabels() {
    cg.addLabel(staticCall, Hotness.HOT);
    MutableCallGraph copy = cg.copy();

    assertEquals(Set.of(Hotness.HOT), copy.getLabels(staticCall));

    copy.addLabel(staticCall, Hotness.COLD);
    copy.addLabel(virtualCall, Hotness.COLD);
    assertEquals(Set.of(Hotness.HOT), cg.getLabels(staticCall));
    assertTrue(cg.getLabels(virtualCall).isEmpty());

    cg.addLabel(virtualCall, Hotness.HOT);
    assertEquals(Set.of(Hotness.COLD), copy.getLabels(virtualCall));
  }

  @Test
  public void testDotExportContainsLabels() {
    String unlabeledEdge = cg.toDotEdge(staticCall).toString();
    assertEquals(
        "\""
            + mainMethod
            + "\"->\""
            + staticDispatchB
            + "\"[label=\""
            + staticCall.getLineNumber()
            + "\"]",
        unlabeledEdge);

    cg.addLabel(staticCall, Hotness.HOT);
    cg.addLabel(staticCall, new TaintFlow("\"in\"", "out"));
    String labeledEdge = cg.toDotEdge(staticCall).toString();
    assertEquals(
        "\""
            + mainMethod
            + "\"->\""
            + staticDispatchB
            + "\"[label=\""
            + staticCall.getLineNumber()
            + ": HOT, TaintFlow[source=\\\"in\\\", sink=out]\"]",
        labeledEdge);
    assertTrue(cg.exportAsDot().anyMatch(labeledEdge::equals));
  }

  @Test
  public void testToStringContainsLabels() {
    cg.addLabel(staticCall, Hotness.HOT);
    String cgString = cg.toString();
    assertTrue(cgString.contains("\tto " + staticDispatchB + " [HOT]"));
    assertTrue(cgString.contains("\tfrom " + mainMethod + " [HOT]"));
    assertTrue(cgString.contains("\tto " + virtualDispatchB + "\n"));
  }
}
