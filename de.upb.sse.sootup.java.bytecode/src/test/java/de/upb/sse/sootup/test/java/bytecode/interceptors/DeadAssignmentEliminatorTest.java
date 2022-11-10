package de.upb.sse.sootup.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import de.upb.sse.sootup.core.graph.ImmutableStmtGraph;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.basic.NoPositionInformation;
import de.upb.sse.sootup.core.jimple.basic.StmtPositionInfo;
import de.upb.sse.sootup.core.jimple.basic.Trap;
import de.upb.sse.sootup.core.jimple.common.constant.IntConstant;
import de.upb.sse.sootup.core.jimple.common.stmt.Stmt;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.types.PrimitiveType;
import de.upb.sse.sootup.core.util.ImmutableUtils;
import de.upb.sse.sootup.java.bytecode.interceptors.DeadAssignmentEliminator;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;

public class DeadAssignmentEliminatorTest {

  @Test
  public void testRemoveDeadAssignment() {
    Body.BodyBuilder testBuilder = createBody(false);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    ImmutableStmtGraph expectedGraph = testBody.getStmtGraph();
    ImmutableStmtGraph actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.nodes().size() - 1, actualGraph.nodes().size());
  }

  @Test
  public void testNoModification() {
    Body.BodyBuilder testBuilder = createBody(true);
    Body testBody = testBuilder.build();
    new DeadAssignmentEliminator().interceptBody(testBuilder);
    Body processedBody = testBuilder.build();
    ImmutableStmtGraph expectedGraph = testBody.getStmtGraph();
    ImmutableStmtGraph actualGraph = processedBody.getStmtGraph();

    assertEquals(expectedGraph.nodes().size(), actualGraph.nodes().size());
  }

  private static Body.BodyBuilder createBody(boolean essentialOption) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");

    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", objectType);
    Local c = JavaJimple.newLocal("c", PrimitiveType.getInt());

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(a, noPositionInfo);

    Set<Local> locals = ImmutableUtils.immutableSet(a, b, c);

    List<Trap> traps = new ArrayList<>();

    Body.BodyBuilder builder = Body.builder();
    builder.setStartingStmt(strToA);
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));

    if (essentialOption) {
      Stmt newToB = JavaJimple.newAssignStmt(b, JavaJimple.newNewExpr(objectType), noPositionInfo);
      builder.addFlow(strToA, newToB);
      builder.addFlow(newToB, ret);
    } else {
      Stmt intToC = JavaJimple.newAssignStmt(c, IntConstant.getInstance(42), noPositionInfo);
      builder.addFlow(strToA, intToC);
      builder.addFlow(intToC, ret);
    }
    builder.setLocals(locals);
    builder.setTraps(traps);
    builder.setPosition(NoPositionInformation.getInstance());

    return builder;
  }
}
