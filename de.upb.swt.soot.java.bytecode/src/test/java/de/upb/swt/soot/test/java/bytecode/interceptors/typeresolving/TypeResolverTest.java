package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.types.VoidType;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.AugEvalFunction;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.BytecodeHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.TypeResolver;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TypeResolverTest {

  JavaView view;
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  Body body;
  ClassType classType;
  SootClass clazz;
  AugEvalFunction function;
  BytecodeHierarchy hierarchy;

  VoidType voidType = VoidType.getInstance();

  @Before
  public void setup() {
    String baseDir = "../shared-test-resources/TypeResolverTestSuite/CastCounterTest/";
    JavaClassPathAnalysisInputLocation analysisInputLocation =
        new JavaClassPathAnalysisInputLocation(baseDir);
    JavaClassPathAnalysisInputLocation rtJar =
        new JavaClassPathAnalysisInputLocation(System.getProperty("java.home") + "/lib/rt.jar");
    JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(analysisInputLocation)
            .addInputLocation(rtJar)
            .build();
    view = project.createOnDemandView();

    classType = factory.getClassType("CastCounterDemos");
    clazz = view.getClass(classType).get();
    function = new AugEvalFunction(view);
    hierarchy = new BytecodeHierarchy(view);
  }

  @Test
  public void testInvokeStmt() {
    setMethod("invokeStmt", voidType, Collections.emptyList());
    Body.BodyBuilder builder = new Body.BodyBuilder(this.body, Collections.emptySet());
    TypeResolver resolver = new TypeResolver(builder, view);
    Body newbody = resolver.getBody();
    Assert.assertEquals(
        newbody.toString(),
        "{\n"
            + "    CastCounterDemos l0;\n"
            + "    Sub1 l1, $stack4;\n"
            + "    byte l2;\n"
            + "    Sub2 l3, $stack5;\n"
            + "\n"
            + "\n"
            + "    l0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    $stack4 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $stack4.<Sub1: void <init>()>();\n"
            + "\n"
            + "    l1 = $stack4;\n"
            + "\n"
            + "    l2 = 1;\n"
            + "\n"
            + "    $stack5 = new Sub2;\n"
            + "\n"
            + "    specialinvoke $stack5.<Sub2: void <init>()>();\n"
            + "\n"
            + "    l3 = $stack5;\n"
            + "\n"
            + "    virtualinvoke l1.<Super1: void m(int,Sub2)>(l2, l3);\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }

  @Test
  public void testAssignStmt() {
    setMethod("assignStmt", voidType, Collections.emptyList());
    Body.BodyBuilder builder = new Body.BodyBuilder(this.body, Collections.emptySet());
    TypeResolver resolver = new TypeResolver(builder, view);
    Body newbody = resolver.getBody();
    Assert.assertEquals(
        newbody.toString(),
        "{\n"
            + "    CastCounterDemos l0;\n"
            + "    Super1[] l1;\n"
            + "    Super1 l2;\n"
            + "    Sub1 $stack3;\n"
            + "\n"
            + "\n"
            + "    l0 := @this: CastCounterDemos;\n"
            + "\n"
            + "    l1 = newarray (Super1)[10];\n"
            + "\n"
            + "    $stack3 = new Sub1;\n"
            + "\n"
            + "    specialinvoke $stack3.<Sub1: void <init>()>();\n"
            + "\n"
            + "    l1[0] = $stack3;\n"
            + "\n"
            + "    l2 = l1[2];\n"
            + "\n"
            + "    return;\n"
            + "}\n");
  }

  private void setMethod(String methodName, Type returnType, List<Type> paras) {
    MethodSignature methodSignature =
        factory.getMethodSignature(classType, methodName, returnType, paras);
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    body = method.getBody();
  }
}
