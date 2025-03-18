package sootup.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.Value;
import sootup.core.jimple.common.constant.Constant;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.ref.Ref;
import sootup.core.jimple.visitor.ReplaceUseRefVisitor;
import sootup.core.signatures.FieldSignature;
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.language.JavaJimple;
import sootup.java.core.types.JavaClassType;

/**
 * @author Zun Wang
 */
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

    assertEquals(expectedUses, newRef.getUses().collect(Collectors.toList()));
    expectedUses.clear();

    // replace constant index with newUse
    visitor = new ReplaceUseRefVisitor();
    visitor.init(conIndex, conNewIndex);
    ref = javaJimple.newArrayRef(base, conIndex);
    ref.accept(visitor);
    newRef = visitor.getResult();

    expectedUses.add(base);
    expectedUses.add(conNewIndex);
    assertEquals(expectedUses, newRef.getUses().collect(Collectors.toList()));
    expectedUses.clear();

    // replace local index with newUse
    visitor = new ReplaceUseRefVisitor();
    visitor.init(localIndex, localNewIndex);
    ref = javaJimple.newArrayRef(base, localIndex);
    ref.accept(visitor);
    newRef = visitor.getResult();

    expectedUses.add(base);
    expectedUses.add(localNewIndex);
    assertEquals(expectedUses, newRef.getUses().collect(Collectors.toList()));
    expectedUses.clear();

    // no matched use
    ref = javaJimple.newArrayRef(base, conIndex);
    ref.accept(visitor);
    assertEquals(visitor.getResult(), ref);
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

    assertEquals(expectedUses, newRef.getUses().collect(Collectors.toList()));

    // no matched use
    ref = JavaJimple.newInstanceFieldRef(localIndex, fieldSignature);
    ref.accept(visitor);
    assertEquals(visitor.getResult(), ref);
  }
}
