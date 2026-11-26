package sootup.apk.frontend;

/*-
 * #%L
 * SootUp
 * %%
 * Copyright (C) 2022 - 2024 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott, Palaniappan Muthuraman, Marcus Hüwe and others
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.apk.frontend.interceptors.DexReturnInliner;
import sootup.apk.frontend.main.AndroidVersionInfo;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.interceptor.BodyInterceptor;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.Local;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.FallsThroughStmt;
import sootup.core.jimple.common.stmt.JGotoStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.types.PrimitiveType;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/** Tests that branches of dex code that share a return get their own returns. */
public class DexReturnInlinerTest {

  private final StmtPositionInfo pos = StmtPositionInfo.getNoStmtPositionInfo();
  private final Local p = Jimple.newLocal("p", PrimitiveType.getInt());
  private final Local x = Jimple.newLocal("x", PrimitiveType.getBoolean());

  private Body.BodyBuilder builder() {
    Body.BodyBuilder builder = Body.builder();
    builder.setLocals(new HashSet<>(Arrays.asList(p, x)));
    builder.setMethodSignature(
        new JavaIdentifierFactory()
            .getMethodSignature("a.B", "test", "boolean", Collections.emptyList()));
    return builder;
  }

  /** The input has a single return that both branches reach */
  private static void assertOneSharedReturn(MutableControlFlowGraph graph, Stmt ret) {
    assertEquals(
        List.of(ret),
        graph.getNodes().stream()
            .filter(s -> s instanceof JReturnStmt)
            .collect(Collectors.toList()));
    assertEquals(2, graph.predecessors(ret).size());
  }

  private static List<Stmt> returns(Body body) {
    return body.getStmts().stream()
        .filter(s -> s instanceof JReturnStmt)
        .collect(Collectors.toList());
  }

  /**
   *
   *
   * <pre>
   *   if p == 0 goto l2;
   *   x = 1;
   *   goto l1;
   * l2:
   *   x = 0;
   * l1:
   *   return x;
   * </pre>
   */
  @Test
  public void gotoToReturnBecomesReturn() {
    Body.BodyBuilder builder = builder();
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    BranchingStmt ifStmt = Jimple.newIfStmt(Jimple.newEqExpr(p, IntConstant.getInstance(0)), pos);
    FallsThroughStmt x1 = Jimple.newAssignStmt(x, IntConstant.getInstance(1), pos);
    BranchingStmt jump = Jimple.newGotoStmt(pos);
    FallsThroughStmt x0 = Jimple.newAssignStmt(x, IntConstant.getInstance(0), pos);
    Stmt ret = Jimple.newReturnStmt(x, pos);
    graph.setStartingStmt(ifStmt);
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, x1);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, x0);
    graph.putEdge(x1, jump);
    graph.putEdge(jump, JGotoStmt.BRANCH_IDX, ret);
    graph.putEdge(x0, ret);
    assertOneSharedReturn(graph, ret);

    new DexReturnInliner().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body body = builder.build();

    assertFalse(body.getStmts().stream().anyMatch(s -> s instanceof JGotoStmt));
    List<Stmt> returns = returns(body);
    assertEquals(2, returns.size());
    for (Stmt r : returns) {
      assertEquals(1, body.getControlFlowGraph().predecessors(r).size());
    }
  }

  /**
   *
   *
   * <pre>
   *   x = 0;
   *   if p == 0 goto l1;
   *   x = 1;
   * l1:
   *   return x;
   * </pre>
   */
  @Test
  public void conditionalJumpToSharedReturnGetsOwnReturn() {
    Body.BodyBuilder builder = builder();
    MutableControlFlowGraph graph = builder.getControlFlowGraph();
    FallsThroughStmt x0 = Jimple.newAssignStmt(x, IntConstant.getInstance(0), pos);
    BranchingStmt ifStmt = Jimple.newIfStmt(Jimple.newEqExpr(p, IntConstant.getInstance(0)), pos);
    FallsThroughStmt x1 = Jimple.newAssignStmt(x, IntConstant.getInstance(1), pos);
    Stmt ret = Jimple.newReturnStmt(x, pos);
    graph.setStartingStmt(x0);
    graph.putEdge(x0, ifStmt);
    graph.putEdge(ifStmt, JIfStmt.FALSE_BRANCH_IDX, x1);
    graph.putEdge(ifStmt, JIfStmt.TRUE_BRANCH_IDX, ret);
    graph.putEdge(x1, ret);
    assertOneSharedReturn(graph, ret);

    new DexReturnInliner().interceptBody(builder, new JavaView(Collections.emptyList()));
    Body body = builder.build();

    List<Stmt> returns = returns(body);
    assertEquals(2, returns.size());
    for (Stmt r : returns) {
      assertEquals(1, body.getControlFlowGraph().predecessors(r).size());
    }
    // the fall-through keeps the original return, the jump goes to a copy
    List<Stmt> branches = body.getControlFlowGraph().successors(ifStmt);
    assertEquals(x1, branches.get(JIfStmt.FALSE_BRANCH_IDX));
    assertTrue(branches.get(JIfStmt.TRUE_BRANCH_IDX) instanceof JReturnStmt);
    assertNotSame(ret, branches.get(JIfStmt.TRUE_BRANCH_IDX));
    assertEquals(List.of(x1), body.getControlFlowGraph().predecessors(ret));
  }

  /** After the default dex interceptors, the inliner leaves no jump to a return in an APK. */
  @Test
  public void noJumpsToReturnsAreLeftInAnApk() {
    Path apkPath = Paths.get("src/test/resources/LocationLeak1.apk");
    JavaView view =
        new JavaView(
            new ApkAnalysisInputLocation(
                apkPath, new AndroidVersionInfo(apkPath, ""), withReturnInliner()));
    long jumpsToReturns =
        view.getClasses()
            .flatMap(c -> c.getMethods().stream())
            .filter(m -> m.hasBody())
            .map(m -> m.getBody().getControlFlowGraph())
            .flatMap(
                g ->
                    g.getNodes().stream()
                        .filter(s -> s instanceof JGotoStmt)
                        .filter(
                            s ->
                                g.successors(s).get(0) instanceof JReturnStmt
                                    || g.successors(s).get(0) instanceof JReturnVoidStmt))
            .count();
    assertEquals(0, jumpsToReturns);
  }

  private static List<BodyInterceptor> withReturnInliner() {
    List<BodyInterceptor> interceptors =
        new ArrayList<>(DexBodyInterceptors.Default.bodyInterceptors());
    interceptors.add(new DexReturnInliner());
    return interceptors;
  }
}
