package sootup.core.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Trap;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.*;
import sootup.core.model.Body;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;
import sootup.core.types.VoidType;
import sootup.core.util.printer.BriefStmtPrinter;

public class ControlFlowGraphGetTrapsTest {

  private final ClassType throwableSig = classType("java.lang", "Throwable");
  private final ClassType ioExceptionSig = classType("java.io", "IOException");

  private static ClassType classType(String pkg, String cls) {
    PackageName p = SignatureInterner.getPackageName(pkg);
    return new ClassType() {
      @Override
      public String getFullyQualifiedName() {
        return getPackageName() + "." + getClassName();
      }

      @Override
      public String getClassName() {
        return cls;
      }

      @Override
      public PackageName getPackageName() {
        return p;
      }
    };
  }

  private static JNopStmt nop() {
    return new JNopStmt(StmtPositionInfo.getNoStmtPositionInfo());
  }

  private static JReturnVoidStmt ret() {
    return new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());
  }

  private static JIdentityStmt handler(ClassType type) {
    return new JIdentityStmt(
        Jimple.newLocal("ex", type),
        new JCaughtExceptionRef(type),
        StmtPositionInfo.getNoStmtPositionInfo());
  }

  private static List<Trap> printerTraps(ControlFlowGraph<?> graph) {
    BriefStmtPrinter printer = new BriefStmtPrinter();
    printer.buildTraps(graph);
    return printer.getTraps();
  }

  @Test
  public void getTraps_matchesPrinterOracle() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt s1 = nop();
    JReturnVoidStmt s2 = ret();
    JIdentityStmt h1 = handler(throwableSig);
    JReturnVoidStmt h2 = ret();

    graph.addBlock(Collections.singletonList(s1));
    graph.addBlock(Collections.singletonList(s2));
    graph.setStartingStmt(s1);
    graph.addExceptionalEdge(s1, throwableSig, h1);
    graph.addBlock(Collections.singletonList(h1));
    graph.addBlock(Collections.singletonList(h2));
    graph.putEdge(s1, s2);
    graph.putEdge(h1, h2);

    List<Trap> traps = graph.getTraps();
    assertEquals(1, traps.size());
    // same traps in the same order as the printer-based route
    assertEquals(printerTraps(graph), traps);
    assertEquals(s1, traps.get(0).getBeginStmt());
    assertEquals(s2, traps.get(0).getEndStmt());
    assertEquals(h1, traps.get(0).getHandlerStmt());
    assertEquals(throwableSig, traps.get(0).getExceptionType());
  }

  @Test
  public void getTraps_multipleTraps() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt s1 = nop();
    JNopStmt s2 = nop();
    JReturnVoidStmt s3 = ret();
    JIdentityStmt h1 = handler(throwableSig);
    JReturnVoidStmt h1End = ret();
    JIdentityStmt h2 = handler(ioExceptionSig);
    JReturnVoidStmt h2End = ret();

    graph.addBlock(Collections.singletonList(s1));
    graph.addBlock(Collections.singletonList(s2));
    graph.addBlock(Collections.singletonList(s3));
    graph.setStartingStmt(s1);
    graph.addExceptionalEdge(s1, throwableSig, h1);
    graph.addExceptionalEdge(s2, ioExceptionSig, h2);
    graph.putEdge(s1, s2);
    graph.putEdge(s2, s3);
    graph.addBlock(Collections.singletonList(h1));
    graph.addBlock(Collections.singletonList(h1End));
    graph.putEdge(h1, h1End);
    graph.addBlock(Collections.singletonList(h2));
    graph.addBlock(Collections.singletonList(h2End));
    graph.putEdge(h2, h2End);

    List<Trap> traps = graph.getTraps();
    assertEquals(2, traps.size());
    assertEquals(printerTraps(graph), traps);
    assertEquals(throwableSig, traps.get(0).getExceptionType());
    assertEquals(ioExceptionSig, traps.get(1).getExceptionType());
  }

  @Test
  public void getTraps_noTraps() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt s1 = nop();
    JReturnVoidStmt s2 = ret();
    graph.addBlock(Collections.singletonList(s1));
    graph.addBlock(Collections.singletonList(s2));
    graph.setStartingStmt(s1);
    graph.putEdge(s1, s2);

    assertTrue(graph.getTraps().isEmpty());
  }

  @Test
  public void getTraps_trapEndingAtGraphEndHasSyntheticEndStmt() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JReturnVoidStmt s1 = ret();
    JIdentityStmt h1 = handler(throwableSig);

    // s1 is the last visited block and still carries the exceptional edge to h1, so the trap is
    // closed with a synthetic end stmt that is not part of the graph
    graph.addBlock(Collections.singletonList(s1));
    graph.setStartingStmt(s1);
    graph.addBlock(Collections.singletonList(h1), Collections.singletonMap(throwableSig, h1));
    graph.addExceptionalEdge(s1, throwableSig, h1);

    List<Trap> traps = graph.getTraps();
    assertEquals(1, traps.size());
    Trap trap = traps.get(0);
    assertEquals(s1, trap.getBeginStmt());
    assertEquals(h1, trap.getHandlerStmt());
    // the synthetic end stmt is not part of the graph
    // (note: it is a fresh instance per aggregation, so it can't be compared to the printer's
    // one by identity)
    assertFalse(graph.containsNode(trap.getEndStmt()));
  }

  @Test
  public void getTraps_copyCtorPreservesTraps() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt s1 = nop();
    JReturnVoidStmt s2 = ret();
    JIdentityStmt h1 = handler(throwableSig);
    JReturnVoidStmt h2 = ret();

    graph.addBlock(Collections.singletonList(s1));
    graph.addBlock(Collections.singletonList(s2));
    graph.setStartingStmt(s1);
    graph.addExceptionalEdge(s1, throwableSig, h1);
    graph.addBlock(Collections.singletonList(h1));
    graph.addBlock(Collections.singletonList(h2));
    graph.putEdge(s1, s2);
    graph.putEdge(h1, h2);

    // the copy ctor used by Body.builder(body, modifiers) must keep the traps
    MutableBlockControlFlowGraph copy = new MutableBlockControlFlowGraph(graph);
    assertEquals(graph.getTraps(), copy.getTraps());
  }

  @Test
  public void bodyGetTraps_delegatesToControlFlowGraph() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt s1 = nop();
    JReturnVoidStmt s2 = ret();
    JIdentityStmt h1 = handler(throwableSig);
    JReturnVoidStmt h2 = ret();

    graph.addBlock(Collections.singletonList(s1));
    graph.addBlock(Collections.singletonList(s2));
    graph.setStartingStmt(s1);
    graph.addExceptionalEdge(s1, throwableSig, h1);
    graph.addBlock(Collections.singletonList(h1));
    graph.addBlock(Collections.singletonList(h2));
    graph.putEdge(s1, s2);
    graph.putEdge(h1, h2);

    Body body =
        Body.builder(graph)
            .setMethodSignature(
                SignatureInterner.getMethodSignature(
                    classType("test", "C"), "m", Collections.emptyList(), VoidType.getInstance()))
            .setPosition(NoPositionInformation.getInstance())
            .build();

    assertEquals(graph.getTraps(), body.getTraps());
    assertEquals(1, body.getTraps().size());
  }
}
