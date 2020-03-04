package de.upb.swt.soot.test.callgraph.typehierarchy.methoddispatchtestcase;


import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.MethodDispatchBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Iterator;

import static org.junit.Assert.*;

/** @author: Hasitha Rajapakse **/

@Category(Java8Test.class)
public class AbstractDispatchTest extends MethodDispatchBase {
    @Test
    public void method() {
        ClassType sootClassTypeA = getClassType("A");
        ClassType sootClassTypeB = getClassType("B");
        SootClass sootClassA = customTestWatcher.getView().getClass(sootClassTypeA).get();
        SootClass sootClassB = customTestWatcher.getView().getClass(sootClassTypeB).get();

        assertEquals(sootClassA.getSuperclass(),sootClassB.getSuperclass());

        SootMethod sootMethodA1 = sootClassA.getMethod(identifierFactory.getMethodSignature("method", sootClassTypeA, "void", Collections.emptyList())).get();
        SootMethod sootMethodB1 = sootClassB.getMethod(identifierFactory.getMethodSignature("method", sootClassTypeB, "void", Collections.emptyList())).get();

        SootMethod sootMethodA2 = sootClassA.getMethod(identifierFactory.getMethodSignature("intmethod", sootClassTypeA, "int", Collections.emptyList())).get();
        SootMethod sootMethodB2 = sootClassB.getMethod(identifierFactory.getMethodSignature("intmethod", sootClassTypeB, "int", Collections.emptyList())).get();

        SootMethod sootMethodA3 = sootClassA.getMethod(identifierFactory.getMethodSignature("parammethod", sootClassTypeA, "void", Collections.singletonList("int"))).get();
        SootMethod sootMethodB3 = sootClassB.getMethod(identifierFactory.getMethodSignature("parammethod", sootClassTypeB, "void", Collections.singletonList("int"))).get();

        SootMethod sootMethodA4 = sootClassA.getMethod(identifierFactory.getMethodSignature("combmethod", sootClassTypeA, "int", Collections.singletonList("int"))).get();
        SootMethod sootMethodB4 = sootClassB.getMethod(identifierFactory.getMethodSignature("combmethod", sootClassTypeB, "int", Collections.singletonList("int"))).get();

        assertNotEquals(sootMethodA1.getBody(),sootMethodB1.getBody());
        assertNotEquals(sootMethodA2.getBody(),sootMethodB2.getBody());
        assertNotEquals(sootMethodA3.getBody(),sootMethodB3.getBody());
        assertNotEquals(sootMethodA4.getBody(),sootMethodB4.getBody());
    }
}
