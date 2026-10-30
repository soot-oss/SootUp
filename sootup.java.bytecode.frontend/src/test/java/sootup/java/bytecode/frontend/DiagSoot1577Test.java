package sootup.java.bytecode.frontend;

/*-
 * #%L
 * Soot - a J*va Optimization Framework
 * %%
 * Copyright (C) 2022 Kadiray Karakaya, Markus Schmidt, Jonas Klauke, Stefan Schott,
 *                     Palaniappan Muthuraman, Marcus Hüwe and others
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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.graph.ControlFlowGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.Body;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.java.bytecode.frontend.inputlocation.ClassFileBasedAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.views.JavaView;

/**
 * Test that exception handlers that share code do not produce duplicate @caughtexception references
 * on the same execution path. This verifies the fix for the issue where inline exception handler
 * merging could produce a second @caughtexception identity statement reachable from a trap handler
 * via normal control flow.
 */
public class DiagSoot1577Test {

  @Test
  public void testNoDuplicateCaughtExceptionOnPath() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("src/test/resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    MethodSignature sig =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                "cn.com.chinatelecom.account.api.c.g",
                "h",
                "int",
                Arrays.asList("android.content.Context"));
    Body body = view.getMethod(sig).get().getBody();

    assertNoDuplicateCaughtExceptionOnPath(body);
  }

  @Test
  public void testInlineHandlerBodyHasCaughtExceptionStmt() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("src/test/resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    MethodSignature sig =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature(
                "cn.com.chinatelecom.account.api.c.g",
                "h",
                "int",
                Arrays.asList("android.content.Context"));
    Body body = view.getMethod(sig).get().getBody();

    // The method must have at least one @caughtexception identity statement
    List<Stmt> caughtExceptionStmts =
        body.getStmts().stream()
            .filter(DiagSoot1577Test::isCaughtExceptionStmt)
            .collect(Collectors.toList());
    assertFalse(caughtExceptionStmts.isEmpty(), "Expected at least one @caughtexception stmt");

    // Each @caughtexception stmt must have at least one successor (it should be followed
    // by handler code, not be a dead end)
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();
    for (Stmt stmt : caughtExceptionStmts) {
      assertFalse(
          cfg.successors(stmt).isEmpty(), "@caughtexception stmt has no successors: " + stmt);
    }
  }

  @Test
  public void testAllMethodsInClassConvertSuccessfully() {
    AnalysisInputLocation inputLocation =
        new ClassFileBasedAnalysisInputLocation(
            Paths.get("src/test/resources/soot-1577/g.class"),
            "cn.com.chinatelecom.account.api.c",
            SourceType.Application);

    JavaView view = new JavaView(inputLocation);

    // Converting all methods should not throw any exceptions
    view.getClasses()
        .findFirst()
        .get()
        .getMethods()
        .forEach(
            method -> {
              Body body = method.getBody();
              assertNotNull(body, "Body should not be null for " + method.getSignature());
              assertNoDuplicateCaughtExceptionOnPath(body);
            });
  }

  @Test
  public void testNestedTryCatchNoDuplicateCaughtException() {
    JavaClassPathAnalysisInputLocation inputLocation =
        new JavaClassPathAnalysisInputLocation(
            "src/test/resources/bugfixes/", SourceType.Application, Collections.emptyList());
    JavaView view = new JavaView(Collections.singletonList(inputLocation));

    // NestedTryCatchFlow has complex nested try-catch with multiple exception types
    // and handler sharing - verify no duplicate @caughtexception on any path
    SootMethod method =
        view.getMethod(
                view.getIdentifierFactory()
                    .parseMethodSignature("<NestedTryCatchFlow: int test_nested_try_catch_2(int)>"))
            .orElse(null);
    assertNotNull(method);
    Body body = method.getBody();
    assertNoDuplicateCaughtExceptionOnPath(body);
  }

  private void assertNoDuplicateCaughtExceptionOnPath(Body body) {
    ControlFlowGraph<?> cfg = body.getControlFlowGraph();

    for (Stmt stmt : body.getStmts()) {
      if (isCaughtExceptionStmt(stmt)) {
        // BFS from this caught exception stmt to check if another caught exception stmt
        // is reachable through normal (non-exceptional) control flow
        Set<Stmt> visited = new HashSet<>();
        Queue<Stmt> queue = new LinkedList<>();
        for (Stmt succ : cfg.successors(stmt)) {
          queue.add(succ);
        }
        while (!queue.isEmpty()) {
          Stmt current = queue.poll();
          if (!visited.add(current)) {
            continue;
          }
          assertFalse(
              isCaughtExceptionStmt(current),
              "Found duplicate @caughtexception on execution path: "
                  + stmt
                  + " -> ... -> "
                  + current);
          for (Stmt succ : cfg.successors(current)) {
            queue.add(succ);
          }
        }
      }
    }
  }

  private static boolean isCaughtExceptionStmt(Stmt stmt) {
    return stmt instanceof JIdentityStmt
        && ((JIdentityStmt) stmt).getRightOp() instanceof JCaughtExceptionRef;
  }
}
