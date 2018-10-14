package de.upb.soot.core;

import static org.junit.Assert.assertEquals;

import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.jimple.common.type.VoidType;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    IView view = new JavaView(null);
    view.addRefType(new RefType(view, "java.lang.String"));
    Type type = RefType.getInstance("java.lang.String");
    SootMethod dummyMainMethod = new SootMethod(view, "main", Arrays.asList(new Type[] { type }), VoidType.getInstance(),
        Modifier.PUBLIC | Modifier.STATIC);
    SootClass mainClass = new SootClass(view, "MainClass");
    mainClass.addMethod(dummyMainMethod);
    assertEquals("<MainClass: void main(java.lang.String)>", dummyMainMethod.getSignature());
  }
}
