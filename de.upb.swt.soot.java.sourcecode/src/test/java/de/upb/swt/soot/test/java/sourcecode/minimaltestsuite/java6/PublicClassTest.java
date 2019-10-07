/// ** @author: Hasitha Rajapakse */
// package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6.VisibilityModifierTest;
//
// import static org.junit.Assert.assertEquals;
// import static org.junit.Assert.assertTrue;
//
// import categories.Java8Test;
// import de.upb.swt.soot.core.frontend.ClassSource;
// import de.upb.swt.soot.core.model.SootMethod;
// import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
// import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
// import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.LoadClassesWithWala;
// import java.util.Collections;
// import java.util.Optional;
// import org.junit.Before;
// import org.junit.Test;
// import org.junit.experimental.categories.Category;
//
// @Category(Java8Test.class)
// public class PublicClassTest {
//  private String srcDir = "src/test/resources/minimaltestsuite/java6/VisibilityModifier/";
//  private String className = "PublicClass";
//  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();
//
//  @Before
//  public void loadClasses() {
//    loadClassesWithWala.classLoader(srcDir, className);
//  }
//
//  @Test
//  public void publicClassTest() {
//    Optional<ClassSource> sc =
//        loadClassesWithWala.loader.getClassSource(loadClassesWithWala.declareClassSig);
//    ClassSource classSource = sc.get();
//    assertEquals(classSource.resolveModifiers().toString(), "[PUBLIC]");
//  }
//
//  @Test
//  public void publicMethodTest() {
//    Optional<SootMethod> m =
//        WalaClassLoaderTestUtils.getSootMethod(
//            loadClassesWithWala.loader,
//            loadClassesWithWala.identifierFactory.getMethodSignature(
//                "publicMethod",
//                loadClassesWithWala.declareClassSig,
//                "void",
//                Collections.emptyList()));
//    assertTrue(m.isPresent());
//    SootMethod method = m.get();
//    Utils.print(method, false);
//    assertTrue(method.isPublic());
//  }
//
//  @Test
//  public void privateMethodTest() {
//    Optional<SootMethod> m =
//        WalaClassLoaderTestUtils.getSootMethod(
//            loadClassesWithWala.loader,
//            loadClassesWithWala.identifierFactory.getMethodSignature(
//                "privateMethod",
//                loadClassesWithWala.declareClassSig,
//                "void",
//                Collections.emptyList()));
//    assertTrue(m.isPresent());
//    SootMethod method = m.get();
//    Utils.print(method, false);
//    assertTrue(method.isPrivate());
//  }
//
//  @Test
//  public void protectedMethodTest() {
//    Optional<SootMethod> m =
//        WalaClassLoaderTestUtils.getSootMethod(
//            loadClassesWithWala.loader,
//            loadClassesWithWala.identifierFactory.getMethodSignature(
//                "protectedMethod",
//                loadClassesWithWala.declareClassSig,
//                "void",
//                Collections.emptyList()));
//    assertTrue(m.isPresent());
//    SootMethod method = m.get();
//    Utils.print(method, false);
//    assertTrue(method.isProtected());
//  }
//
//  @Test
//  public void noModifierMethodTest() {
//    Optional<SootMethod> m =
//        WalaClassLoaderTestUtils.getSootMethod(
//            loadClassesWithWala.loader,
//            loadClassesWithWala.identifierFactory.getMethodSignature(
//                "noModifierMethod",
//                loadClassesWithWala.declareClassSig,
//                "void",
//                Collections.emptyList()));
//    assertTrue(m.isPresent());
//    SootMethod method = m.get();
//    Utils.print(method, false);
//    assertEquals(method.getModifiers().toString(), "[]");
//  }
// }

/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6;

import categories.Java8Test;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.MinimalTestSuiteBase;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class PublicClassTest extends MinimalTestSuiteBase{

    private String methodName;
    private String methodSignature;
    private List<String> jimpleLines;

    @Test
    public void defaultTest() {
        HashMap<String, HashMap<String, Object>> methodList = setValues();
        Set<String> methodListKeys = methodList.keySet();

        for (String methodListKey : methodListKeys) {
            methodName = methodListKey;
            HashMap<String, Object> mv = methodList.get(methodListKey);
            methodSignature = (String) mv.get("methodSignature");
        }
    }

    @Override
    public MethodSignature getMethodSignature() {
        return identifierFactory.getMethodSignature(
                methodName, getDeclaredClassSignature(), methodSignature, Collections.emptyList());
    }

    @Override
    public List<String> getJimpleLines() {
        return jimpleLines;
    }

    public HashMap<String, HashMap<String, Object>> setValues() {
        HashMap<String, HashMap<String, Object>> methodList = new HashMap<>();
        HashMap<String, Object> methodValues = new HashMap<>();

        methodValues.put("methodSignature", "void");
        methodList.put("publicMethod", methodValues);

        methodValues = new HashMap<>();
        methodValues.put("methodSignature", "void");
        methodList.put("privateMethod", methodValues);

        methodValues = new HashMap<>();
        methodValues.put("methodSignature", "void");
        methodList.put("protectedMethod", methodValues);

        methodValues = new HashMap<>();
        methodValues.put("methodSignature", "void");
        methodList.put("noModifierMethod", methodValues);

        return methodList;
    }
}
