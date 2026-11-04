package sootup.core.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableBlockControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.ref.JParameterRef;
import sootup.core.jimple.common.ref.JThisRef;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.PackageName;
import sootup.core.signatures.SignatureInterner;
import sootup.core.types.ClassType;
import sootup.core.types.PrimitiveType;
import sootup.core.types.VoidType;

public class BodyIdentityLocalsTest {

  private static final StmtPositionInfo POS = StmtPositionInfo.getNoStmtPositionInfo();

  private final ClassType classType =
      new ClassType() {
        @Override
        public String getFullyQualifiedName() {
          return "a.Test";
        }

        @Override
        public String getClassName() {
          return "Test";
        }

        @Override
        public PackageName getPackageName() {
          return SignatureInterner.getPackageName("a");
        }
      };

  private final MethodSignature signature =
      SignatureInterner.getMethodSignature(
          classType,
          SignatureInterner.getMethodSubSignature(
              "m",
              VoidType.getInstance(),
              Arrays.asList(PrimitiveType.getInt(), PrimitiveType.getInt())));

  private final Local thisLocal = Jimple.newLocal("this", classType);
  private final Local p0 = Jimple.newLocal("p0", PrimitiveType.getInt());
  private final Local p1 = Jimple.newLocal("p1", PrimitiveType.getInt());
  private final JIdentityStmt thisStmt = new JIdentityStmt(thisLocal, new JThisRef(classType), POS);
  private final JIdentityStmt p0Stmt =
      new JIdentityStmt(p0, new JParameterRef(PrimitiveType.getInt(), 0), POS);
  private final JIdentityStmt p1Stmt =
      new JIdentityStmt(p1, new JParameterRef(PrimitiveType.getInt(), 1), POS);

  private Body build(MutableBlockControlFlowGraph graph) {
    return Body.builder(graph)
        .setMethodSignature(signature)
        .setLocals(new HashSet<>(Arrays.asList(thisLocal, p0, p1)))
        .build();
  }

  /** this, p0, p1, followed by many nops */
  private MutableBlockControlFlowGraph leadingIdentityStmts() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(thisStmt);
    graph.putEdge(thisStmt, p0Stmt);
    graph.putEdge(p0Stmt, p1Stmt);
    FallsThroughStmt last = p1Stmt;
    for (int i = 0; i < 100; i++) {
      JNopStmt nop = new JNopStmt(POS);
      graph.putEdge(last, nop);
      last = nop;
    }
    graph.putEdge(last, new JReturnVoidStmt(POS));
    return graph;
  }

  @Test
  public void findsLeadingIdentityLocals() {
    Body body = build(leadingIdentityStmts());

    assertSame(thisStmt, body.getThisStmt());
    assertSame(thisLocal, body.getThisLocal());
    assertSame(p0, body.getParameterLocal(0));
    assertSame(p1, body.getParameterLocal(1));
    assertEquals(Arrays.asList(p0, p1), new java.util.ArrayList<>(body.getParameterLocals()));
    assertThrows(IllegalArgumentException.class, () -> body.getParameterLocal(2));
    assertThrows(IllegalArgumentException.class, () -> body.getParameterLocal(-1));
  }

  @Test
  public void leadingIdentityLocalsDoNotIterateTheBody() {
    MutableBlockControlFlowGraph graph = spy(leadingIdentityStmts());
    Body body = build(graph);
    clearInvocations(graph);

    body.getThisLocal();
    body.getParameterLocal(0);
    body.getParameterLocal(1);
    body.getParameterLocals();

    verify(graph, never()).iterator();
  }

  @Test
  public void identityLocalsThatAreNotAtTheBeginningAreRejected() {
    // an invalid body: the identity stmts must come first (see IdentityStmtsValidator)
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    JNopStmt nop = new JNopStmt(POS);
    graph.setStartingStmt(nop);
    graph.putEdge(nop, thisStmt);
    graph.putEdge(thisStmt, p0Stmt);
    graph.putEdge(p0Stmt, p1Stmt);
    graph.putEdge(p1Stmt, new JReturnVoidStmt(POS));
    graph = spy(graph);
    Body body = build(graph);
    clearInvocations(graph);

    assertNull(body.getThisLocal());
    assertThrows(IllegalStateException.class, () -> body.getParameterLocal(1));
    assertThrows(IllegalStateException.class, body::getParameterLocals);
    verify(graph, never()).iterator();
  }

  @Test
  public void parameterLocalsOutOfOrderAreRejected() {
    // an invalid body: the parameters must be assigned in order (see IdentityStmtsValidator)
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(thisStmt);
    graph.putEdge(thisStmt, p1Stmt);
    graph.putEdge(p1Stmt, p0Stmt);
    graph.putEdge(p0Stmt, new JReturnVoidStmt(POS));
    Body body = build(graph);

    assertSame(thisLocal, body.getThisLocal());
    assertThrows(IllegalStateException.class, () -> body.getParameterLocal(0));
    assertThrows(IllegalStateException.class, () -> body.getParameterLocal(1));
    assertThrows(IllegalStateException.class, body::getParameterLocals);
  }

  @Test
  public void staticBodyHasNoThisLocal() {
    MutableBlockControlFlowGraph graph = new MutableBlockControlFlowGraph();
    graph.setStartingStmt(p0Stmt);
    graph.putEdge(p0Stmt, p1Stmt);
    graph.putEdge(p1Stmt, new JReturnVoidStmt(POS));
    graph = spy(graph);
    Body body = build(graph);
    clearInvocations(graph);

    assertNull(body.getThisLocal());
    assertSame(p0, body.getParameterLocal(0));
    assertSame(p1, body.getParameterLocal(1));
    assertEquals(Arrays.asList(p0, p1), new java.util.ArrayList<>(body.getParameterLocals()));
    verify(graph, never()).iterator();
  }
}
