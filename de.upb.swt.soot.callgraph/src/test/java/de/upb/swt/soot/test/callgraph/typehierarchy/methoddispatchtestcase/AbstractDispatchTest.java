package de.upb.swt.soot.test.callgraph.typehierarchy.methoddispatchtestcase;


import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.MethodDispatchResolver;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.MethodDispatchBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import static org.junit.Assert.*;

/** @author: Hasitha Rajapakse **/

@Category(Java8Test.class)
public class AbstractDispatchTest extends MethodDispatchBase {
    @Test
    public void method() {

        ClassType sootClassTypeA = getClassType("A");
        ClassType sootClassTypeB = getClassType("B");
        ClassType sootClassTypeC = getClassType("C");
        ClassType sootClassTypeAbstract = getClassType("AbstractClass");

        MethodSignature sootMethodA = identifierFactory.getMethodSignature("method", sootClassTypeA, "void", Collections.emptyList());
        MethodSignature sootMethodB = identifierFactory.getMethodSignature("method", sootClassTypeB, "void", Collections.emptyList());
        MethodSignature sootMethodC = identifierFactory.getMethodSignature("method", sootClassTypeC, "void", Collections.emptyList());

        Set<MethodSignature> candidatesAbstract = MethodDispatchResolver.resolveAbstractDispatch(customTestWatcher.getView(), identifierFactory.getMethodSignature("method", sootClassTypeAbstract, "void", Collections.emptyList()));
        assertTrue(candidatesAbstract.contains(sootMethodA));
        assertTrue(candidatesAbstract.contains(sootMethodB));
        assertTrue(candidatesAbstract.contains(sootMethodC));

        Set<MethodSignature> candidatesSuper = MethodDispatchResolver.resolveAbstractDispatch(customTestWatcher.getView(), identifierFactory.getMethodSignature("method", sootClassTypeA, "void", Collections.emptyList()));
        assertTrue(candidatesSuper.contains(sootMethodB));
        assertTrue(candidatesSuper.contains(sootMethodC));

    }
}
