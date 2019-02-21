package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.java.WalaClassLoader;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;

import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

/**
 * 
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class InvokeTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private JavaClassSignature declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    sigFactory = new DefaultSignatureFactory();
  }

  @Test
  public void testInvokeSpecialInstanceInit() {
    declareClassSig = sigFactory.getClassSignature("InvokeSpecial");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("specialInvokeInstanceInit", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeSpecialPrivateMethod() {
    declareClassSig = sigFactory.getClassSignature("InvokeSpecial");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("specialInvokePrivateMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeSpecialSuperClassMethod() {
    declareClassSig = sigFactory.getClassSignature("InvokeSpecial");
    Optional<SootMethod> m = loader.getSootMethod(sigFactory.getMethodSignature("specialInvokeSupperClassMethod",
        declareClassSig, "java.lang.String", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeStatic() {
    declareClassSig = sigFactory.getClassSignature("InvokeStatic");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("<clinit>", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual1() {
    declareClassSig = sigFactory.getClassSignature("InvokeVirtual");
    Optional<SootMethod> m = loader.getSootMethod(
        sigFactory.getMethodSignature("equals", declareClassSig, "boolean", Collections.singletonList("InvokeVirtual")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual2() {
    declareClassSig = sigFactory.getClassSignature("InvokeVirtual");
    Optional<SootMethod> m = loader
        .getSootMethod(sigFactory.getMethodSignature("interfaceMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual3() {
    declareClassSig = sigFactory.getClassSignature("InvokeVirtual");
    Optional<SootMethod> m
        = loader.getSootMethod(sigFactory.getMethodSignature("doStuf", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }
}
