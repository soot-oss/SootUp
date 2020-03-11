package de.upb.swt.soot.test.callgraph.typehierarchy.testcase;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.test.callgraph.typehierarchy.JavaTypeHierarchyBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;


import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

import static org.junit.Assert.*;


@Category(Java8Test.class)
public class ClassInheritanceWithAdditionalMethodTest extends JavaTypeHierarchyBase {

    @Test
    public void method(){

        ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
        /**Test: {@link java.lang.Object} is superclass of "SuperClass"*/
        assertEquals(typeHierarchy.superClassOf(getClassType("SuperClass")),
                     getClassType("java.lang.Object"));

        /**Test: "SuperClass" is a subclass of {@link java.lang.Object}*/
        Set<ClassType> subClassSet = new HashSet<ClassType>();
        subClassSet = typeHierarchy.subclassesOf(getClassType("java.lang.Object"));
        assertTrue(subClassSet.contains(getClassType("SuperClass")));

        /**Test: "ClassInheritanceWithAdditionalMethod"'s superclass is "SuperClass"*/
        assertEquals(typeHierarchy.superClassOf(getClassType("ClassInheritanceWithAdditionalMethod")),
                     getClassType("SuperClass"));

        /**Test: "ClassInheritanceWithAdditionalMethod" is subclass of "SuperClass"*/
        subClassSet = typeHierarchy.subclassesOf(getClassType("SuperClass"));
        assertTrue(subClassSet.contains(getClassType("ClassInheritanceWithAdditionalMethod")));

        /**Test: "ClassInheritanceWithAdditionalMethod" has additional method. */
        SootClass sootClass = (SootClass) customTestWatcher.getView().getClass(
                                            customTestWatcher.getView().getIdentifierFactory().getClassType(
                                                customTestWatcher.getClassName())).get();
        SootClass superClass = (SootClass) customTestWatcher.getView().getClass(sootClass.getSuperclass().get()).get();

        Set<SootMethod> methodsSetOfSootClass = sootClass.getMethods();
        Set<SootMethod> methodsSetOfSuperClass = superClass.getMethods();

        Set<String> methodsStrings = methodsSetOfSootClass.stream()
                                        .map(sootMethod -> sootMethod.getName() + sootMethod.getParameterTypes())
                                        .collect(Collectors.toSet());

        Set<String> methodsStingsInSuperClass = methodsSetOfSuperClass.stream()
                                                .map(sootMethod -> sootMethod.getName() + sootMethod.getParameterTypes())
                                                .collect(Collectors.toSet());

        methodsStrings.addAll(methodsStingsInSuperClass);

        assertNotEquals(methodsStrings, methodsStingsInSuperClass);
    }
}