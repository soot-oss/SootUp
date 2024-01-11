package sootup.java.bytecode.inputlocation;

/*-
 * #%L
 * Soot
 * %%
 * Copyright (C) 06.06.2018 Manuel Benz
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 2.1 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

import categories.Java8Test;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.ClassModifier;
import sootup.core.model.FieldModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.ClassType;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.signatures.ModuleSignature;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.types.ModuleJavaClassType;
import sootup.java.core.views.JavaModuleView;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

/**
 * @author Manuel Benz created on 06.06.18
 * @author Kaustubh Kelkar updated on 16.04.2020
 */
@Category(Java8Test.class)
public class PathBasedAnalysisInputLocationTest extends AnalysisInputLocationTest {

    @Test
    public void multiReleaseJar() {
        final ClassType classType =
                getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");
        final ClassType classType2 =
                getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

        final JavaView view_min = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, new JavaLanguage(1)));
        final JavaView view_8 = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, new JavaLanguage(8)));
        final JavaView view_9 = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, new JavaLanguage(9)));
        final JavaView view_10 = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, new JavaLanguage(10)));
        final JavaView view_max = new JavaView(new MultiReleaseJarAnalysisInputLocation(mrj, SourceType.Application, new JavaLanguage(Integer.MAX_VALUE)));

        // for java10
        Assert.assertEquals(
                "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
                view_10.getClass(classType).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_10.getClass(classType2).get().getClassSource().getSourcePath().toString());

        // assert that method is correctly resolved
        Assert.assertTrue(
                view_10
                        .getClass(classType)
                        .get()
                        .getMethod(
                                getIdentifierFactory()
                                        .getMethodSubSignature(
                                                "printVersion",
                                                getIdentifierFactory().getType("void"),
                                                Collections.emptyList()))
                        .get()
                        .getBody()
                        .toString()
                        .contains("java 9"));

        // for java 9
        Assert.assertEquals(
                "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
                view_9.getClass(classType).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());

        // for java 8
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Utility.class",
                view_8.getClass(classType).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
        // assert that method is correctly resolved to base
        Assert.assertTrue(
                view_8
                        .getClass(classType)
                        .get()
                        .getMethod(
                                getIdentifierFactory()
                                        .getMethodSubSignature(
                                                "printVersion",
                                                getIdentifierFactory().getType("void"),
                                                Collections.emptyList()))
                        .get()
                        .getBody()
                        .toString()
                        .contains("java 8"));

        // for max int
        Assert.assertEquals(
                "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
                view_max.getClass(classType).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_max.getClass(classType2).get().getClassSource().getSourcePath().toString());

        // for min int
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Utility.class",
                view_min.getClass(classType).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_min.getClass(classType2).get().getClassSource().getSourcePath().toString());
    }

    @Test
    public void modularMultiReleaseJar() {
        final ClassType utilityNoModule =
                getIdentifierFactory().getClassType("de.upb.swt.multirelease.Utility");

        final ModuleJavaClassType utilityModule =
                JavaModuleIdentifierFactory.getInstance()
                        .getClassType("de.upb.swt.multirelease/de.upb.swt.multirelease.Utility");

        final ClassType classType2 =
                getIdentifierFactory().getClassType("de.upb.swt.multirelease.Main");

        MultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation8 = new MultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, new JavaLanguage(8));
        final JavaView view_8 = new JavaView(Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation8));


        MultiReleaseJarAnalysisInputLocation moduleMultiReleaseJarAnalysisInputLocation9 = new MultiReleaseJarAnalysisInputLocation(mmrj, SourceType.Application, new JavaLanguage(9));
        final JavaModuleView view_9 = new JavaModuleView(Collections.emptyList(), Collections.singletonList(moduleMultiReleaseJarAnalysisInputLocation9));

        ModuleSignature moduleSignature =
                JavaModuleIdentifierFactory.getModuleSignature("de.upb.swt.multirelease");

        Assert.assertEquals(Collections.singleton(moduleSignature), view_9.getNamedModules());

        Assert.assertTrue(view_9.getModuleInfo(moduleSignature).isPresent());

        Assert.assertEquals(1, view_9.getModuleClasses(moduleSignature).size());

        Assert.assertEquals(
                "de.upb.swt.multirelease.Utility",
                view_9.getModuleClasses(moduleSignature).stream()
                        .findAny()
                        .get()
                        .getType()
                        .getFullyQualifiedName());

        // for java 9
        Assert.assertEquals(
                "/META-INF/versions/9/de/upb/swt/multirelease/Utility.class",
                view_9.getClass(utilityModule).get().getClassSource().getSourcePath().toString());
        // different class will be returned if no module is specified
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Utility.class",
                view_9.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_9.getClass(classType2).get().getClassSource().getSourcePath().toString());
        // assert that method is correctly resolved to base
        Assert.assertTrue(
                view_9
                        .getClass(utilityModule)
                        .get()
                        .getMethod(
                                getIdentifierFactory()
                                        .getMethodSubSignature(
                                                "printVersion",
                                                getIdentifierFactory().getType("void"),
                                                Collections.emptyList()))
                        .get()
                        .getBody()
                        .toString()
                        .contains("java 9"));

        // for java 8
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Utility.class",
                view_8.getClass(utilityNoModule).get().getClassSource().getSourcePath().toString());
        assertFalse(view_8.getClass(utilityModule).isPresent());
        Assert.assertEquals(
                "/de/upb/swt/multirelease/Main.class",
                view_8.getClass(classType2).get().getClassSource().getSourcePath().toString());
        // assert that method is correctly resolved to base
        Assert.assertTrue(
                view_8
                        .getClass(utilityNoModule)
                        .get()
                        .getMethod(
                                getIdentifierFactory()
                                        .getMethodSubSignature(
                                                "printVersion",
                                                getIdentifierFactory().getType("void"),
                                                Collections.emptyList()))
                        .get()
                        .getBody()
                        .toString()
                        .contains("java 8"));
    }

    @Test
    public void testApk() {
        PathBasedAnalysisInputLocation pathBasedNamespace = new ApkAnalysisInputLocation(apk, SourceType.Application);
        final ClassType mainClass =
                getIdentifierFactory().getClassType("de.upb.futuresoot.fields.MainActivity");
        testClassReceival(pathBasedNamespace, Collections.singletonList(mainClass), 1392);
    }

    @Test
    public void testSingleClass() {
        PathBasedAnalysisInputLocation pathBasedNamespace = new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(cls,"", SourceType.Application);
        ArrayList<ClassType> sigs = new ArrayList<>();
        sigs.add(getIdentifierFactory().getClassType("Employee"));
        testClassReceival(pathBasedNamespace, sigs, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSingleClassDoesNotExist() {
        PathBasedAnalysisInputLocation pathBasedNamespace =
                PathBasedAnalysisInputLocation.create(
                        Paths.get("NonExisting.class"), SourceType.Application);
    }

    @Test
    public void testSingleClassWPackageName() {
        AnalysisInputLocation pathBasedNamespace = new PathBasedAnalysisInputLocation.ClassFileBasedAnalysisInputLocation(Paths.get("../shared-test-resources/ClassWithPackageName.class"),"ClassesPackageName", SourceType.Application);
        ArrayList<ClassType> sigs = new ArrayList<>();
        sigs.add(getIdentifierFactory().getClassType("ClassesPackageName.ClassWithPackageName"));
        testClassReceival(pathBasedNamespace, sigs, 1);
    }

    @Test
    public void testJar() {
        PathBasedAnalysisInputLocation pathBasedNamespace =
                PathBasedAnalysisInputLocation.create(jar, SourceType.Application);
        ArrayList<ClassType> sigs = new ArrayList<>();
        sigs.add(getIdentifierFactory().getClassType("Employee", "ds"));
        sigs.add(getIdentifierFactory().getClassType("MiniApp"));
        testClassReceival(pathBasedNamespace, sigs, 6);
    }

    @Test
    public void testWar() {
        PathBasedAnalysisInputLocation pathBasedNamespace =
                PathBasedAnalysisInputLocation.create(war, SourceType.Application);
        final ClassType warClass1 = getIdentifierFactory().getClassType("SimpleWarRead");
        testClassReceival(pathBasedNamespace, Collections.singletonList(warClass1), 19);
    }

    @Test
    public void testClassInWar() {

        String warFile = "../shared-test-resources/java-warApp/dummyWarApp.war";

        assertTrue("File " + warFile + " not found.", new File(warFile).exists());

        // Get the view
        JavaView view = new JavaView(new JavaClassPathAnalysisInputLocation(warFile));

        assertEquals(19, view.getClasses().size());

        // Create java class signature
        ClassType utilsClassSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

        // Resolve signature to `SootClass`
        JavaSootClass utilsClass = view.getClass(utilsClassSignature).get();

        // Parse sub-signature for "setEmpSalary" method
        MethodSubSignature optionalToStreamMethodSubSignature =
                JavaIdentifierFactory.getInstance().parseMethodSubSignature("void setEmpSalary(int)");

        // Get method for sub-signature
        JavaSootMethod foundMethod = utilsClass.getMethod(optionalToStreamMethodSubSignature).get();
        assertNotNull(foundMethod.getBody());

        // Print method
        assertTrue("setEmpSalary".equalsIgnoreCase(foundMethod.getName()));
        assertEquals("void", foundMethod.getReturnType().toString());
        assertEquals(1, foundMethod.getParameterCount());
        assertTrue(
                foundMethod.getParameterTypes().stream().anyMatch(type -> "int".equals(type.toString())));

        // Parse sub-signature for "empName" field
        FieldSubSignature nameFieldSubSignature =
                JavaIdentifierFactory.getInstance().parseFieldSubSignature("java.lang.String empName");

        // Create the class signature
        JavaClassType classSignature = view.getIdentifierFactory().getClassType("Employee", "ds");

        JavaSootField field =
                new JavaSootField(
                        JavaIdentifierFactory.getInstance()
                                .getFieldSignature(classSignature, nameFieldSubSignature),
                        Collections.singleton(FieldModifier.PUBLIC),
                        null,
                        NoPositionInformation.getInstance());

        // Build a soot class
        JavaSootClass c =
                new JavaSootClass(
                        new OverridingJavaClassSource(
                                new EagerInputLocation(),
                                null,
                                classSignature,
                                null,
                                null,
                                null,
                                Collections.singleton(field),
                                Collections.emptySet(),
                                null,
                                EnumSet.of(ClassModifier.PUBLIC),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()),
                        SourceType.Application);

        assertEquals("java.lang.String", c.getField(nameFieldSubSignature).get().getType().toString());
        assertEquals("empName", c.getField(nameFieldSubSignature).get().getName());
    }

    void runtimeContains(View view, String classname, String packageName) {
        final ClassType sig = getIdentifierFactory().getClassType(classname, packageName);
        assertTrue(sig + " is not found in rt.jar", view.getClass(sig).isPresent());
    }

    @Test
    public void testRuntimeJar() {
        PathBasedAnalysisInputLocation pathBasedNamespace = new DefaultRTJarAnalysisInputLocation();

        JavaView v = new JavaView(pathBasedNamespace);

        // test some standard jre classes
        runtimeContains(v, "Object", "java.lang");
        runtimeContains(v, "List", "java.util");
        runtimeContains(v, "Map", "java.util");
        runtimeContains(v, "ArrayList", "java.util");
        runtimeContains(v, "HashMap", "java.util");
        runtimeContains(v, "Collection", "java.util");
        runtimeContains(v, "Comparator", "java.util");
    }

    /**
     * Test for JavaClassPathAnalysisInputLocation. Specifying jar file with source type as Library.
     * Expected - All input classes are of source type Library.
     */
    @Test
    public void testInputLocationLibraryMode() {
        JavaView view = new JavaView(new DefaultRTJarAnalysisInputLocation());

        Collection<SootClass> classes = new HashSet<>(); // Set to track the classes to check

        for (SootClass aClass : view.getClasses()) {
            if (!aClass.isLibraryClass()) {
                classes.add(aClass);
            }
        }

        assertEquals("User Defined class found, expected none", 0, classes.size());
    }
}
