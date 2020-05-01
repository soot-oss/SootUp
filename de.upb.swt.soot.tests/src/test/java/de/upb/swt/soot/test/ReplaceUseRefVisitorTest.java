package de.upb.swt.soot.test;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.basic.Value;
import de.upb.swt.soot.core.jimple.common.ref.Ref;
import de.upb.swt.soot.core.jimple.visitor.ReplaceUseRefVisitor;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.language.JavaJimple;
import de.upb.swt.soot.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class ReplaceUseRefVisitorTest {
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType arrayType = factory.getClassType("Array");

  Local base = JavaJimple.newLocal("base", arrayType);
  Local index = JavaJimple.newLocal("index", intType);
  Local newUse = JavaJimple.newLocal("newUse", intType);

  FieldSignature fieldSignature = new FieldSignature(arrayType, "field", intType);
  MethodSignature methodeWithOutParas =
      new MethodSignature(arrayType, "invokeExpr", Collections.emptyList(), intType);
  StmtPositionInfo noStmtPositionInfo = StmtPositionInfo.createNoStmtPositionInfo();

  /** Test use replacing in case JArrayRef. */
  @Test
  public void testCaseArrayRef() {

    // replace base with newUse
    ReplaceUseRefVisitor visitor = new ReplaceUseRefVisitor(base, newUse);
    Ref ref = javaJimple.newArrayRef(base, index);
    ref.accept(visitor);
    Ref newRef = visitor.getNewRef();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newUse);
    expectedUses.add(index);

    assertTrue(newRef.getUses().equals(expectedUses));

    // replace base two times
    ref = javaJimple.newArrayRef(base, base);
    ref.accept(visitor);
    newRef = visitor.getNewRef();

    expectedUses.set(1, newUse);
    assertTrue(newRef.getUses().equals(expectedUses));

    // no matched use
    ref = javaJimple.newArrayRef(index, index);
    ref.accept(visitor);
    newRef = visitor.getNewRef();

    assertTrue(newRef.equivTo(ref));
  }

  /** Test use replacing in case JInstanceFieldRef. */
  @Test
  public void testCaseInstanceFieldRef() {

    ReplaceUseRefVisitor visitor = new ReplaceUseRefVisitor(base, newUse);

    // replace base with newUse
    Ref ref = JavaJimple.newInstanceFieldRef(base, fieldSignature);
    ref.accept(visitor);
    Ref newRef = visitor.getNewRef();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newUse);

    assertTrue(newRef.getUses().equals(expectedUses));

    // no matched use
    ref = JavaJimple.newInstanceFieldRef(index, fieldSignature);
    ref.accept(visitor);
    newRef = visitor.getNewRef();

    assertTrue(newRef.equals(ref));
  }
}
