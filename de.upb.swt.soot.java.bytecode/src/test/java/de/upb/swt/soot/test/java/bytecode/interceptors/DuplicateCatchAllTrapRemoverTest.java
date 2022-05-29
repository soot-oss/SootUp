package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.*;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Body.BodyBuilder;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.util.ImmutableUtils;
import de.upb.swt.soot.java.bytecode.interceptors.DuplicateCatchAllTrapRemover;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
public class DuplicateCatchAllTrapRemoverTest {

  /**
   * Test the correct removal of duplicate catch all traps. The correct transformation is the
   * following:
   *
   * <p>Before: Trap: begin: a = "str" - end: b = (java.lang.String) a - handler: goto [?= b =
   * (java.lang.String) a] Trap: begin: b = "str" - end: b = (java.lang.String) a - handler: return
   * b Trap: begin: b = "str" - end: b = (java.lang.String) a - handler: return b
   *
   * <p>After: Trap: begin: a = "str" - end: b = (java.lang.String) a - handler: goto [?= b =
   * (java.lang.String) a] Trap: begin: b = "str" - end: b = (java.lang.String) a - handler: return
   * b
   */
  @Test
  public void testRemoveDuplicate() {
    Body.BodyBuilder originalBuilder = createBody(true);
    Body originalBody = originalBuilder.build();

    new DuplicateCatchAllTrapRemover().interceptBody(originalBuilder);
    Body processedBody = originalBuilder.build();

    Collection<Trap> originalTraps = originalBody.getTraps();
    Collection<Trap> processedTraps = processedBody.getTraps();

    assertEquals(3, originalTraps.size());
    assertEquals(2, processedTraps.size());
    for (Trap trap : processedTraps) {
      assertTrue(originalTraps.contains(trap));
    }
  }

  /** Tests the correct handling of a {@link Body} without duplicate catch all traps. */
  @Test
  public void testRemoveNothing() {
    Body.BodyBuilder originalBuilder = createBody(false);
    Body originalBody = originalBuilder.build();

    new DuplicateCatchAllTrapRemover().interceptBody(originalBuilder);
    Body processedBody = originalBuilder.build();

    assertArrayEquals(originalBody.getTraps().toArray(), processedBody.getTraps().toArray());
  }

  /**
   * Creates a basic {@link Body} for each test case. Depending on the parameter, it adds Traps that
   * should be removed.
   *
   * <p>The {@link Trap}s with the following properties are generated: Trap: begin: a = "str" - end:
   * b = (java.lang.String) a - handler: goto [?= b = (java.lang.String) a] Trap: begin: b = "str" -
   * end: b = (java.lang.String) a - handler: return b Trap: begin: b = "str" - end: b =
   * (java.lang.String) a - handler: return b (only created if containsDuplicate is true)
   *
   * @param containsDuplicate determines whether the Body contains a Trap that should be removed
   * @return the created Body
   */
  private Body.BodyBuilder createBody(boolean containsDuplicate) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType objectType = factory.getClassType("java.lang.Object");
    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", objectType);
    Local b = JavaJimple.newLocal("b", stringType);
    Set<Local> locals = ImmutableUtils.immutableSet(a, b);

    Stmt strToA = JavaJimple.newAssignStmt(a, javaJimple.newStringConstant("str"), noPositionInfo);
    Stmt bToA = JavaJimple.newAssignStmt(b, JavaJimple.newCastExpr(a, stringType), noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    Stmt jump = JavaJimple.newGotoStmt(noPositionInfo);

    List<Trap> traps = new ArrayList<>();
    ExceptionType exceptionType = new ExceptionType();
    Trap trap1 = new Trap(exceptionType, strToA, bToA, jump);
    traps.add(trap1);
    Trap trap2 = new Trap(exceptionType, strToA, bToA, ret);
    traps.add(trap2);
    if (containsDuplicate) {
      Trap trap3 = new Trap(exceptionType, strToA, bToA, ret);
      traps.add(trap3);
    }
    List<Stmt> stmts = ImmutableUtils.immutableList(strToA, jump, bToA, ret);

    BodyBuilder builder = Body.builder();
    for (int i = 0; i < stmts.size() - 1; i++) {
      Stmt from = stmts.get(i);
      Stmt to = stmts.get(i + 1);
      if (i == 0) builder.setStartingStmt(from);
      builder.addFlow(from, to);
    }
    builder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "a.b.c", "void", Collections.emptyList()));
    return builder
        .setLocals(locals)
        .setTraps(traps)
        .setPosition(NoPositionInformation.getInstance());
  }

  private static class ExceptionType extends ClassType {
    @Override
    public boolean isBuiltInClass() {
      return false;
    }

    @Override
    public String getFullyQualifiedName() {
      return "java.lang.Throwable";
    }

    @Override
    public String getClassName() {
      return "Throwable";
    }

    @Override
    public PackageName getPackageName() {
      return new PackageName("java.lang");
    }
  }
}
