package de.upb.soot.minimaltestsuite.java6.VisibilityModifierTest;

import static org.junit.Assert.*;

import categories.Java8Test;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.java.Utils;
import de.upb.soot.frontends.java.WalaClassLoaderTestUtils;
import de.upb.soot.minimaltestsuite.LoadClassesWithWala;
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
