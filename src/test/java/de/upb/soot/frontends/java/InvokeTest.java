package de.upb.soot.frontends.java;

import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.DefaultFactories;
import de.upb.soot.core.SootMethod;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.types.DefaultTypeFactory;
import de.upb.soot.types.JavaClassType;
import java.util.Collections;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class InvokeTest {
  private WalaClassLoader loader;
  private DefaultSignatureFactory sigFactory;
  private DefaultTypeFactory typeFactory;
  private JavaClassType declareClassSig;

  @Before
  public void loadClassesWithWala() {
    String srcDir = "src/test/resources/selected-java-target/";
    loader = new WalaClassLoader(srcDir, null);
    DefaultFactories factories = DefaultFactories.create();
    sigFactory = factories.getSignatureFactory();
    typeFactory = factories.getTypeFactory();
  }

  @Test
  public void testInvokeSpecialInstanceInit() {
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokeInstanceInit", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeSpecialPrivateMethod() {
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokePrivateMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeSpecialSuperClassMethod() {
    declareClassSig = typeFactory.getClassType("InvokeSpecial");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "specialInvokeSupperClassMethod",
                declareClassSig,
                "java.lang.String",
                Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeStatic() {
    declareClassSig = typeFactory.getClassType("InvokeStatic");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "<clinit>", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual1() {
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "equals", declareClassSig, "boolean", Collections.singletonList("InvokeVirtual")));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual2() {
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "interfaceMethod", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }

  @Test
  public void testInvokeVirtual3() {
    declareClassSig = typeFactory.getClassType("InvokeVirtual");
    Optional<SootMethod> m =
        loader.getSootMethod(
            sigFactory.getMethodSignature(
                "doStuf", declareClassSig, "void", Collections.emptyList()));
    assertTrue(m.isPresent());
    SootMethod method = m.get();
    Utils.print(method, false);
  }
}
