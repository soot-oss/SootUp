package de.upb.swt.soot.test.java.sourcecode.typehierarchy.testcase;

import categories.Java8Test;
import de.upb.swt.soot.callgraph.typehierarchy.TypeHierarchy;
import de.upb.swt.soot.callgraph.typehierarchy.ViewTypeHierarchy;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.test.java.sourcecode.typehierarchy.JavaTypeHierarchyBase;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** @author: Hasitha Rajapakse **/

@Category(Java8Test.class)
public class ClassInheritenceTest extends JavaTypeHierarchyBase {
    @Test
    public void method(){
        SootClass sootClass = (SootClass) customTestWatcher.getView().getClass(customTestWatcher.getView().getIdentifierFactory().getClassType(customTestWatcher.getClassName())).get();
        assertTrue(sootClass.hasSuperclass());
        ViewTypeHierarchy typeHierarchy = (ViewTypeHierarchy) TypeHierarchy.fromView(customTestWatcher.getView());
        assertEquals(typeHierarchy.superClassOf(getClassType(customTestWatcher.getClassName())),getClassType("SuperClass"));
    }
}
