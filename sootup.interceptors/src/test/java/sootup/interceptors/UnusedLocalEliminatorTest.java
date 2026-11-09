package sootup.interceptors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.views.JavaView;

public class UnusedLocalEliminatorTest {

  /**
   * Reproduces local variable chain order corruption in UnusedLocalEliminator:
   *
   * <p>Subject pattern: A method body declared with local variable chain [l1, l2Unused, l3, l4].
   * The statements execute uses in order: s1 uses l4, s2 uses l1, s3 uses l3.
   *
   * <p>Previously, UnusedLocalEliminator recreated the local set entirely from statement use/def
   * encounters over CFG nodes. This resulted in locals being reordered to [l4, l1, l3], discarding
   * the declared local variable table chain order [l1, l3, l4].
   *
   * <p>Reordering locals disrupts parameter and local register allocation in bytecode backends.
   * Retaining builder.getLocals() order ensures [l1, l3, l4] is preserved.
   */
  @Test
  public void testPreserveLocalChainOrder() {
    Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
    Local l2Unused = JavaJimple.newLocal("l2Unused", PrimitiveType.getInt());
    Local l3 = JavaJimple.newLocal("l3", PrimitiveType.getInt());
    Local l4 = JavaJimple.newLocal("l4", PrimitiveType.getInt());

    Set<Local> initialLocals = new LinkedHashSet<>();
    initialLocals.add(l1);
    initialLocals.add(l2Unused);
    initialLocals.add(l3);
    initialLocals.add(l4);

    StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
    // Stmts use l4 then l1 then l3 (different from initial declaration order)
    JAssignStmt s1 = Jimple.newAssignStmt(l4, IntConstant.getInstance(4), pos);
    JAssignStmt s2 = Jimple.newAssignStmt(l1, IntConstant.getInstance(1), pos);
    JAssignStmt s3 = Jimple.newAssignStmt(l3, IntConstant.getInstance(3), pos);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("com.example.Test", "foo", "void", Collections.emptyList()));
    builder.setLocals(initialLocals);

    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    cfg.setStartingStmt(s1);
    cfg.putEdge(s1, s2);
    cfg.putEdge(s2, s3);

    UnusedLocalEliminator eliminator = new UnusedLocalEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    List<Local> resultLocals = new ArrayList<>(builder.getLocals());
    assertEquals(3, resultLocals.size());
    // Must preserve initial builder chain order: l1, l3, l4 (with l2Unused removed)
    assertEquals(l1, resultLocals.get(0));
    assertEquals(l3, resultLocals.get(1));
    assertEquals(l4, resultLocals.get(2));
    assertFalse(resultLocals.contains(l2Unused));
  }

  @Test
  public void testUndeclaredReferencedLocalAppendedAtEnd() {
    Local l1 = JavaJimple.newLocal("l1", PrimitiveType.getInt());
    Local lUndeclared = JavaJimple.newLocal("lUndeclared", PrimitiveType.getInt());

    Set<Local> initialLocals = new LinkedHashSet<>();
    initialLocals.add(l1);

    StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
    JAssignStmt s1 = Jimple.newAssignStmt(l1, IntConstant.getInstance(1), pos);
    JAssignStmt s2 = Jimple.newAssignStmt(lUndeclared, IntConstant.getInstance(2), pos);

    Body.BodyBuilder builder = Body.builder();
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("com.example.Test", "bar", "void", Collections.emptyList()));
    builder.setLocals(initialLocals);

    MutableControlFlowGraph cfg = builder.getControlFlowGraph();
    cfg.setStartingStmt(s1);
    cfg.putEdge(s1, s2);

    UnusedLocalEliminator eliminator = new UnusedLocalEliminator();
    eliminator.interceptBody(builder, new JavaView(Collections.emptyList()));

    List<Local> resultLocals = new ArrayList<>(builder.getLocals());
    assertEquals(2, resultLocals.size());
    assertEquals(l1, resultLocals.get(0));
    assertEquals(lUndeclared, resultLocals.get(1));
  }
}
