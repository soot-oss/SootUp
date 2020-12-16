package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.assertEquals;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.StringConstant;
import de.upb.swt.soot.core.jimple.common.expr.JEqExpr;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.java.bytecode.interceptors.ConditionalBranchFolder;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Marcus Nachtigall */
@Category(Java8Test.class)
public class ConditionalBranchFolderTest {

  /**
   * Tests the correct deletion of an if-statement with a constant condition. Transforms from
   *
   * <p>a = "str"; b = a; if(a == b) return a; else return b;
   *
   * <p>to
   *
   * <p>a = "str"; b = a; return a;
   */
  @Test
  public void testUnconditionalBranching() {
    Body.BodyBuilder builder = createBody(true);
    Body originalBody = builder.build();
    new ConditionalBranchFolder().interceptBody(builder);
    Body processedBody = builder.build();

    assertEquals(
        originalBody.getStmtGraph().nodes().size() - 2,
        processedBody.getStmtGraph().nodes().size());
  }

  /**
   * Tests the correct handling of an if-statement with inconstant condition. Considers the
   * following code, but does not change anything:
   *
   * <p>a = "str"; b = "different string"; if(a == b) return a; else return b;
   */
  @Test
  public void testConditionalBranching() {
    Body.BodyBuilder builder = createBody(false);
    Body originalBody = builder.build();
    new ConditionalBranchFolder().interceptBody(builder);
    Body processedBody = builder.build();

    assertEquals(originalBody.getStmtGraph().nodes(), processedBody.getStmtGraph().nodes());
  }

  /**
   * Generates the correct test {@link Body} for the corresponding test case.
   *
   * @param constantCondition indicates, whether the condition is constant.
   * @return the generated {@link Body}
   */
  private static Body.BodyBuilder createBody(boolean constantCondition) {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaJimple javaJimple = JavaJimple.getInstance();
    StmtPositionInfo noPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

    JavaClassType stringType = factory.getClassType("java.lang.String");
    Local a = JavaJimple.newLocal("a", stringType);
    Local b = JavaJimple.newLocal("b", stringType);

    StringConstant stringConstant = javaJimple.newStringConstant("str");
    Stmt strToA = JavaJimple.newAssignStmt(a, stringConstant, noPositionInfo);

    Stmt strToB;
    StringConstant anotherStringConstant = stringConstant;
    if (!constantCondition) {
      anotherStringConstant = javaJimple.newStringConstant("different string");
    }
    strToB = JavaJimple.newAssignStmt(b, anotherStringConstant, noPositionInfo);
    JEqExpr jEqExpr = new JEqExpr(stringConstant, anotherStringConstant);
    Stmt ifStmt = Jimple.newIfStmt(jEqExpr, noPositionInfo);
    Stmt ret = JavaJimple.newReturnStmt(b, noPositionInfo);
    Stmt ret2 = JavaJimple.newReturnStmt(a, noPositionInfo);

    Set<Local> locals = new LinkedHashSet<>();
    locals.add(a);
    locals.add(b);
    List<Trap> traps = Collections.emptyList();

    Body.BodyBuilder bodyBuilder = Body.builder();
    bodyBuilder.setLocals(locals);
    bodyBuilder.setTraps(traps);
    bodyBuilder.setStartingStmt(strToA);
    bodyBuilder.addFlow(strToA, strToB);
    bodyBuilder.addFlow(strToB, ifStmt);
    bodyBuilder.addFlow(ifStmt, ret);
    bodyBuilder.addFlow(ifStmt, ret2);
    bodyBuilder.setMethodSignature(
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "ab.c", "void", Collections.emptyList()));
    return bodyBuilder;
  }
}
