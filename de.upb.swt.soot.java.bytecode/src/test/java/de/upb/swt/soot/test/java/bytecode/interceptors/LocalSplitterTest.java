package de.upb.swt.soot.test.java.bytecode.interceptors;

import static org.junit.Assert.*;

import categories.Java8Test;
import com.google.common.graph.*;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Trap;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.java.bytecode.interceptors.LocalSplitter;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class LocalSplitterTest {

  // Preparation
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  // JavaJimple javaJimple = JavaJimple.getInstance();
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  JavaClassType intType = factory.getClassType("int");
  JavaClassType booleanType = factory.getClassType("boolean");

  // build locals
  Local l0 = JavaJimple.newLocal("l0", intType);
  Local l1 = JavaJimple.newLocal("l1", intType);
  Local l2 = JavaJimple.newLocal("l2", intType);
  Local l3 = JavaJimple.newLocal("l3", intType);
  Local l4 = JavaJimple.newLocal("l4", booleanType);
  Local l0hash1 = JavaJimple.newLocal("l0#1", intType);
  Local l0hash2 = JavaJimple.newLocal("l0#2", intType);
  Local l0hash3 = JavaJimple.newLocal("l0#3", intType);
  Local l1hash2 = JavaJimple.newLocal("l1#2", intType);
  Local l1hash4 = JavaJimple.newLocal("l1#4", intType);

  /**
   * int a = 0; int b = 0; a = a + 1; b = b + 1;
   *
   * <pre>
   * 1. l0 = 0
   * 2. l1 = 1
   * 3. l0 = l0 + 1
   * 4. l1 = l1 + 1
   * 5. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l1#2 = 1
   * 3. l0#3 = l0#1 + 1
   * 4. l1#4 = l1#2 + 1
   * 5. return
   * </pre>
   */
  @Test
  public void testLocalSplitterForMultilocals() {
    /**
    Body body = createMultilocalsBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedMuiltilocalsBody();

    // check newBody's locals
    assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());*/
  }

  /**
   * int a = 0; if(a<0) a = a + 1; else {a = a+ 1; a = a +1;} return a
   *
   * <pre>
   * 1. l0 = 0
   * 2. if l0 >= 0 goto l0 = l0 +1
   * 3. l0 = l0 + 1
   * 4. goto  [?= return l0#2]
   * 5. l0 = l0 + 1
   * 6. l0 = l0 + 1
   * 7. return l0
   * </pre>
   *
   * to:
   * alternation1
   * <pre>
   * 1. l0#1 = 0
   * 2. if l0#1 >= 0 goto l0#3 = l0#1 +1
   * 3. l0#2 = l0#1 + 1
   * 4. goto  [?= return l0]
   * 5. l0#3 = l0#1 + 1
   * 6. l0#2 = l0#3 + 1
   * 7. return l0#2
   * </pre>
   *
   * alternation2
   * <pre>
   * 1. l0#1 = 0
   * 2. if l0#1 >= 0 goto l0#2 = l0#1 +1
   * 3. l0#3 = l0#1 + 1
   * 4. goto  [?= return l0#3]
   * 5. l0#2 = l0#1 + 1
   * 6. l0#3 = l0#2 + 1
   * 7. return l0#3
   * </pre>
   *
   */
  @Test
  public void testLocalSplitterForBinaryBranches() {
    /**
    Body body = createBBBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody1 = createExpectedBBBody1();
    Body expectedBody2 = createExpectedBBBody2();

     // check newBody's locals
    assertLocalsEquiv(expectedBody1.getLocals(), newBody.getLocals());

     // check newBody's first stmt
    assertTrue(expectedBody1.getFirstStmt().equivTo(newBody.getFirstStmt()));

     // check newBody's stmtGraph
    assertStmtGraphEquiv(expectedBody1.getStmtGraph(), expectedBody2.getStmtGraph(), newBody.getStmtGraph());
    */
  }

  /**
   * for(int i = 0; i < 10; i++){ i = i + 1 } transform:
   *
   * <pre>
   * 1. l0 = 0
   * 2. l4 = l0 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0 + 1
   * 5. l0 = l1
   * 6. l2 = l0
   * 7. l3 = l0 + 1
   * 8. l0 = l3
   * 9. goto [?= l4 = l0 < 10]
   * 10. return
   * </pre>
   *
   * to:
   *
   * <pre>
   * 1. l0#1 = 0
   * 2. l4 = l0#1 < 10
   * 3. if l4 == 0 goto return
   * 4. l1 = l0#1 + 1
   * 5. l0#2 = l1
   * 6. l2 = l0#2
   * 7. l3 = l0#2 + 1
   * 8. l0#1 = l3
   * 9. goto [?= l4 = l0#1 < 10]
   * 10. return
   * </pre>
   */
  @Test
  public void testLocalSplitterForLoop() {
    /**
    Body body = createLoopBody();
    LocalSplitter localSplitter = new LocalSplitter();
    Body newBody = localSplitter.interceptBody(body);
    Body expectedBody = createExpectedLoopBody();
    */
    // check newBody's locals
    //assertLocalsEquiv(expectedBody.getLocals(), newBody.getLocals());

    // check newBody's first stmt
    //assertTrue(expectedBody.getFirstStmt().equivTo(newBody.getFirstStmt()));

    // check newBody's stmtGraph
    //assertStmtGraphEquiv(expectedBody.getStmtGraph(), newBody.getStmtGraph());
  }


}
