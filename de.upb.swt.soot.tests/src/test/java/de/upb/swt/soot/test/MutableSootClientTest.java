package de.upb.swt.soot.test;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.OverridingBodySource;
import de.upb.swt.soot.core.inputlocation.AnalysisInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.bytecode.inputlocation.PathBasedAnalysisInputLocation;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootClass;
import de.upb.swt.soot.java.core.JavaSootClassSource;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.MutableJavaView;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;

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
    }

    @Test
    public void methodRemovalTest() {
        ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
        Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
        assertTrue(utilsClassOpt.isPresent());

        SootClass<JavaSootClassSource> utilsClass = utilsClassOpt.get();
        MethodSignature ms = p.getIdentifierFactory().parseMethodSignature("<utils.Operations: void removeDepartment(ds.Department)>");
        Optional<? extends SootMethod> removeDepartmentMethodOpt = utilsClass.getMethod(ms.getSubSignature());
        assertTrue(removeDepartmentMethodOpt.isPresent());

        SootMethod removeDepartmentMethod = removeDepartmentMethodOpt.get();
        assertTrue(utilsClass.getMethods().contains(removeDepartmentMethod));
        mv.removeMethod((JavaSootMethod) removeDepartmentMethod);

        // Need to get a new reference to the class, as the old one now points to a class that is no longer in the view
        Optional<JavaSootClass> updatedUtilsClassOpt = mv.getClass(classType);
        assertTrue(updatedUtilsClassOpt.isPresent());
        SootClass<JavaSootClassSource> updatedUtilsClass = updatedUtilsClassOpt.get();
        assertFalse(updatedUtilsClass.getMethods().contains(removeDepartmentMethod));
    }

    @Test
    public void methodAdditionTest() {
        MethodSignature methodSignature =
                p.getIdentifierFactory()
                        .getMethodSignature("addedMethod", "utils.Operations", "void", Collections.emptyList());
        Body.BodyBuilder bodyBuilder = Body.builder();
        Body body = bodyBuilder
                .setMethodSignature(methodSignature)
                .build();
        JavaSootMethod newMethod = new JavaSootMethod(
                new OverridingBodySource(methodSignature, body),
                methodSignature,
                EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
                Collections.emptyList(),
                Collections.emptyList(),
                NoPositionInformation.getInstance());

        ClassType classType = p.getIdentifierFactory().getClassType("utils.Operations");
        Optional<JavaSootClass> utilsClassOpt = mv.getClass(classType);
        assertTrue(utilsClassOpt.isPresent());

        SootClass<JavaSootClassSource> utilsClass = utilsClassOpt.get();
        assertFalse(utilsClass.getMethods().contains(newMethod));
        mv.addMethod(newMethod);

        // Need to get a new reference to the class, as the old one now points to a class that is no longer in the view
        Optional<JavaSootClass> updatedUtilsClassOpt = mv.getClass(classType);
        assertTrue(updatedUtilsClassOpt.isPresent());
        SootClass<JavaSootClassSource> updatedUtilsClass = updatedUtilsClassOpt.get();
        assertTrue(updatedUtilsClass.getMethods().contains(newMethod));
    }
}
