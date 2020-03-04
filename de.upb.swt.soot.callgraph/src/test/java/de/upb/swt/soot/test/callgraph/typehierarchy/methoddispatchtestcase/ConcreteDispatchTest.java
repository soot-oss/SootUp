package de.upb.swt.soot.test.callgraph.typehierarchy.methoddispatchtestcase;


import categories.Java8Test;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.MethodDispatchBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;

import static org.junit.Assert.*;

/** @author: Hasitha Rajapakse **/

@Category(Java8Test.class)
public class ConcreteDispatchTest extends MethodDispatchBase {
    @Test
    public void method() {
        ClassType sootClassTypeA = getClassType("A");

        SootClass sootClassA = customTestWatcher.getView().getClass(sootClassTypeA).get();
        assertEquals(sootClassA.getSuperclass().get().getClassName(),"ConcreteClass");

        SootClass sootClassSuper = customTestWatcher.getView().getClass(sootClassA.getSuperclass().get()).get();
        ClassType sootClassTypeSuper = getClassType(sootClassSuper.getName());

        SootMethod sootMethodA1 = sootClassA.getMethod(identifierFactory.getMethodSignature("method", sootClassTypeA, "void", Collections.emptyList())).get();
        SootMethod sootMethodSuper1 = sootClassSuper.getMethod(identifierFactory.getMethodSignature("method", sootClassTypeSuper, "void", Collections.emptyList())).get();

        SootMethod sootMethodA2 = sootClassA.getMethod(identifierFactory.getMethodSignature("intmethod", sootClassTypeA, "int", Collections.emptyList())).get();
        SootMethod sootMethodSuper2 = sootClassSuper.getMethod(identifierFactory.getMethodSignature("intmethod", sootClassTypeSuper, "int", Collections.emptyList())).get();

        SootMethod sootMethodA3 = sootClassA.getMethod(identifierFactory.getMethodSignature("parammethod", sootClassTypeA, "void", Collections.singletonList("int"))).get();
        SootMethod sootMethodSuper3 = sootClassSuper.getMethod(identifierFactory.getMethodSignature("parammethod", sootClassTypeSuper, "void", Collections.singletonList("int"))).get();

        SootMethod sootMethodA4 = sootClassA.getMethod(identifierFactory.getMethodSignature("combmethod", sootClassTypeA, "int", Collections.singletonList("int"))).get();
        SootMethod sootMethodSuper4 = sootClassSuper.getMethod(identifierFactory.getMethodSignature("combmethod", sootClassTypeSuper, "int", Collections.singletonList("int"))).get();

        assertNotEquals(sootMethodA1.getBody(),sootMethodSuper1.getBody());
        assertNotEquals(sootMethodA2.getBody(),sootMethodSuper2.getBody());
        assertNotEquals(sootMethodA3.getBody(),sootMethodSuper3.getBody());
        assertNotEquals(sootMethodA4.getBody(),sootMethodSuper4.getBody());
    }
}
