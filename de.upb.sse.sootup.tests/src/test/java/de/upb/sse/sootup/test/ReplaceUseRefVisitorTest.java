package de.upb.sse.sootup.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.sse.sootup.core.jimple.basic.Local;
import de.upb.sse.sootup.core.jimple.basic.Value;
import de.upb.sse.sootup.core.jimple.common.constant.Constant;
import de.upb.sse.sootup.core.jimple.common.constant.IntConstant;
import de.upb.sse.sootup.core.jimple.common.ref.Ref;
import de.upb.sse.sootup.core.jimple.visitor.ReplaceUseRefVisitor;
import de.upb.sse.sootup.core.signatures.FieldSignature;
import de.upb.sse.sootup.java.core.JavaIdentifierFactory;
import de.upb.sse.sootup.java.core.language.JavaJimple;
import de.upb.sse.sootup.java.core.types.JavaClassType;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Zun Wang */
@Category(Java8Test.class)
public class ReplaceUseRefVisitorTest {
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  JavaJimple javaJimple = JavaJimple.getInstance();
  JavaClassType intType = factory.getClassType("int");
  JavaClassType arrayType = factory.getClassType("Array");

  Local base = JavaJimple.newLocal("old_base", arrayType);
  Local newBase = JavaJimple.newLocal("new_base", arrayType);

  Constant conIndex = IntConstant.getInstance(0);
  Constant conNewIndex = IntConstant.getInstance(1);

  Local localIndex = JavaJimple.newLocal("index", intType);
  Local localNewIndex = JavaJimple.newLocal("newIndex", intType);

  FieldSignature fieldSignature = new FieldSignature(arrayType, "field", intType);

  /** Test use replacing in case JArrayRef. */
  @Test
  public void testCaseArrayRef() {

    // replace base with newUse
    ReplaceUseRefVisitor visitor = new ReplaceUseRefVisitor();
    visitor.init(base, newBase);
    Ref ref = javaJimple.newArrayRef(base, conIndex);
    ref.accept(visitor);
    Ref newRef = visitor.getResult();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newBase);
    expectedUses.add(conIndex);

    assertEquals(newRef.getUses(), expectedUses);
    expectedUses.clear();

    // replace constant index with newUse
    visitor = new ReplaceUseRefVisitor();
    visitor.init(conIndex, conNewIndex);
    ref = javaJimple.newArrayRef(base, conIndex);
    ref.accept(visitor);
    newRef = visitor.getResult();

    expectedUses.add(base);
    expectedUses.add(conNewIndex);
    assertEquals(newRef.getUses(), expectedUses);
    expectedUses.clear();

    // replace local index with newUse
    visitor = new ReplaceUseRefVisitor();
    visitor.init(localIndex, localNewIndex);
    ref = javaJimple.newArrayRef(base, localIndex);
    ref.accept(visitor);
    newRef = visitor.getResult();

    expectedUses.add(base);
    expectedUses.add(localNewIndex);
    assertEquals(newRef.getUses(), expectedUses);
    expectedUses.clear();

    // no matched use
    try {
      ref = javaJimple.newArrayRef(base, conIndex);
      ref.accept(visitor);
      fail("not allowed!");
    } catch (Exception ignore) {
    }
  }

  /** Test use replacing in case JInstanceFieldRef. */
  @Test
  public void testCaseInstanceFieldRef() {

    ReplaceUseRefVisitor visitor = new ReplaceUseRefVisitor();
    visitor.init(base, newBase);

    // replace base with newUse
    Ref ref = JavaJimple.newInstanceFieldRef(base, fieldSignature);
    ref.accept(visitor);
    Ref newRef = visitor.getResult();

    List<Value> expectedUses = new ArrayList<>();
    expectedUses.add(newBase);

    assertEquals(newRef.getUses(), expectedUses);

    // no matched use
    try {
      ref = JavaJimple.newInstanceFieldRef(localIndex, fieldSignature);
      ref.accept(visitor);
      fail("not allowed!");
    } catch (Exception ignore) {
    }
  }
}