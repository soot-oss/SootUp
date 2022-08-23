package de.upb.swt.soot.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.SootClassSource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.model.SourceType;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootClassSource;
import de.upb.swt.soot.java.core.OverridingJavaClassSource;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.MutableJavaView;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.file.Path;
import java.nio.file.Paths;

@Category(Java8Test.class)
public class MutableSootClientTest {
    static JavaProject p;
    MutableJavaView mv;

    @BeforeClass
    public static void setupProject() {
        Path pathToJar = Paths.get("../shared-test-resources/java-miniapps/MiniApp.jar");
        AnalysisInputLocation<JavaSootClass> cpBased =
                PathBasedAnalysisInputLocation.createForClassContainer(pathToJar, SourceType.Application);
        p = JavaProject.builder(new JavaLanguage(8)).addInputLocation(cpBased).build();
    }

    @Before
    public void setupMutableView() {
        mv = p.createMutableFullView();
    }

    @Test
    public void classRemovalTest() {
        int classesBeforeSize = mv.getClasses().size();
        ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
        mv.removeClass(classType);
        int classesAfterSize = mv.getClasses().size();

        assertTrue(classesBeforeSize > classesAfterSize);

//        Set<? extends JavaSootMethod> methods = tv.getMethods();
//        MethodSignature tvSig = p.getIdentifierFactory().getMethodSignature("transientVariable", classType, "void", Collections.emptyList());
//        methods.removeIf(method -> method.getSignature().equals(tvSig));

    }

    @Test
    public void classAdditionTest() {
        int classesBeforeSize = mv.getClasses().size();
        ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");



        int classesAfterSize = mv.getClasses().size();

        assertTrue(classesBeforeSize > classesAfterSize);
    }
}
