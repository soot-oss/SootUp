/** @author: Hasitha Rajapakse */
package de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.java6.VisibilityModifierTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.swt.soot.core.frontend.ClassSource;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.test.java.sourcecode.frontend.Utils;
import de.upb.swt.soot.test.java.sourcecode.frontend.WalaClassLoaderTestUtils;
import de.upb.swt.soot.test.java.sourcecode.minimaltestsuite.LoadClassesWithWala;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(Java8Test.class)
public class NoModifierClassTest {
  private String srcDir = "src/test/resources/minimaltestsuite/java6/VisibilityModifiers/";
  private String className = "NoModifierClass";
  private LoadClassesWithWala loadClassesWithWala = new LoadClassesWithWala();

  @Before
  public void loadClasses() {
    loadClassesWithWala.classLoader(srcDir, className);
  }

  @Test
  public void noModifierClassTest() {
    Optional<ClassSource> sc =
        loadClassesWithWala.loader.getClassSource(loadClassesWithWala.declareClassSig);
    ClassSource classSource = sc.get();
    assertEquals(classSource.resolveModifiers().toString(), "[]");
  }

  @Test
  public void publicMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "publicMethod",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertTrue(method.isPublic());
  }

  @Test
  public void privateMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "privateMethod",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertTrue(method.isPrivate());
  }

  @Test
  public void protectedMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "protectedMethod",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertTrue(method.isProtected());
  }

  @Test
  public void noModifierMethodTest() {
    Optional<SootMethod> m =
        WalaClassLoaderTestUtils.getSootMethod(
            loadClassesWithWala.loader,
            loadClassesWithWala.identifierFactory.getMethodSignature(
                "noModifierMethod",
                loadClassesWithWala.declareClassSig,
                "void",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
    assertEquals(method.getModifiers().toString(), "[]");
  }
}
