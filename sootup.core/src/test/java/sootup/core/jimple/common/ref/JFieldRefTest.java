package sootup.core.jimple.common.ref;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.TestUtil;
import sootup.core.jimple.common.Local;
import sootup.core.types.PrimitiveType.IntType;

public class JFieldRefTest {

  @Test
  public void testStaticFieldRefEqualsHashCode() {
    JFieldRef a = TestUtil.createDummyStaticFieldRef();
    JFieldRef b = TestUtil.createDummyStaticFieldRef();

    assertEquals(a, b);
    assertEquals(a.hashCode(), b.hashCode());

    Set<JFieldRef> set = new HashSet<>();
    set.add(a);
    assertTrue(set.contains(b));
  }

  @Test
  public void testInstanceFieldRefEqualsHashCode() {
    JInstanceFieldRef a = TestUtil.createDummyInstanceFieldRef();
    JInstanceFieldRef sameBase =
        new JInstanceFieldRef(TestUtil.createDummyLocalForInt(), a.getFieldSignature());
    JInstanceFieldRef otherBase =
        new JInstanceFieldRef(new Local("c", IntType.getInstance()), a.getFieldSignature());

    assertEquals(a, sameBase);
    assertEquals(a.hashCode(), sameBase.hashCode());
    assertNotEquals(a, otherBase);
  }

  @Test
  public void testStaticAndInstanceFieldRefNotEqual() {
    JFieldRef staticRef = TestUtil.createDummyStaticFieldRef();
    JFieldRef instanceRef = TestUtil.createDummyInstanceFieldRef();

    assertEquals(staticRef.getFieldSignature(), instanceRef.getFieldSignature());
    assertNotEquals(staticRef, instanceRef);
    assertNotEquals(instanceRef, staticRef);
  }
}
