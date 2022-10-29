package de.upb.swt.soot.test.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.jimple.basic.Local;
import de.upb.swt.soot.core.jimple.common.constant.IntConstant;
import de.upb.swt.soot.core.jimple.common.ref.JArrayRef;
import de.upb.swt.soot.core.jimple.common.stmt.Stmt;
import de.upb.swt.soot.core.model.Body;
import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.AugEvalFunction;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.BytecodeHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.CastCounter;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.Typing;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.JavaSootMethod;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import de.upb.swt.soot.java.core.views.JavaView;
import java.util.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class CastCounterTest {

  JavaView view;
  JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
  Body body;
  ClassType classType;
  SootClass clazz;
  AugEvalFunction function;
  BytecodeHierarchy hierarchy;

  ClassType super1 = factory.getClassType("Super1");
  ClassType super2 = factory.getClassType("Super2");
  ClassType sub1 = factory.getClassType("Sub1");
  ClassType sub2 = factory.getClassType("Sub2");
  VoidType voidType = VoidType.getInstance();
  ClassType object = factory.getClassType("java.lang.Object");

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

    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getInt());
    map.put("l3", sub2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(body, function, hierarchy);
    int count = counter.getCastCount(typing);
    Assert.assertTrue(count == 0);

    map.replace("l3", super2);
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 1);

    map.replace("l2", PrimitiveType.getLong());
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 3);

    map.replace("l2", PrimitiveType.getInteger127());
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 1);
  }

  @Test
  public void testAssignStmt() {
    setMethod("assignStmt", voidType, Collections.emptyList());

    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", TypeUtils.makeArrayType(super1, 1));
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(body, function, hierarchy);
    int count = counter.getCastCount(typing);
    Assert.assertTrue(count == 0);

    map.replace("l1", object);
    typing = createTyping(map);
    count = counter.getCastCount(typing);
    Assert.assertTrue(count == 5);
  }

  @Test
  public void testInvokeStmtWithNewCasts() {
    setMethod("invokeStmt", voidType, Collections.emptyList());


    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", super1);
    map.put("l2", PrimitiveType.getLong());
    map.put("l3", super2);
    map.put("$stack4", sub1);
    map.put("$stack5", sub2);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(body, function, hierarchy);
    body = counter.insertCastStmts(typing);
    System.out.println(body);
    Assert.assertTrue(counter.getCastCount() == 3);
    Assert.assertTrue(counter.getNewLocals().size() == 4);
    Assert.assertEquals(body.toString(), "{\n" +
            "    unknown l0, l1, l2, l3, $stack4, $stack5;\n" +
            "    integer1 #l0;\n" +
            "    long #l1;\n" +
            "    int #l2;\n" +
            "    Sub2 #l3;\n" +
            "\n" +
            "\n" +
            "    l0 := @this: CastCounterDemos;\n" +
            "\n" +
            "    $stack4 = new Sub1;\n" +
            "\n" +
            "    specialinvoke $stack4.<Sub1: void <init>()>();\n" +
            "\n" +
            "    l1 = $stack4;\n" +
            "\n" +
            "    #l0 = 1;\n" +
            "\n" +
            "    #l1 = (long) #l0;\n" +
            "\n" +
            "    l2 = #l1;\n" +
            "\n" +
            "    $stack5 = new Sub2;\n" +
            "\n" +
            "    specialinvoke $stack5.<Sub2: void <init>()>();\n" +
            "\n" +
            "    l3 = $stack5;\n" +
            "\n" +
            "    #l2 = (int) l2;\n" +
            "\n" +
            "    #l3 = (Sub2) l3;\n" +
            "\n" +
            "    virtualinvoke l1.<Super1: void m(int,Sub2)>(#l2, #l3);\n" +
            "\n" +
            "    return;\n" +
            "}\n");
  }

  @Test
  public void testAssignStmtWithNewCasts() {
    setMethod("assignStmt", voidType, Collections.emptyList());
    System.out.println(body);

    Map<String, Type> map = new HashMap<>();
    map.put("l0", classType);
    map.put("l1", object);
    map.put("l2", super1);
    map.put("$stack3", sub1);
    Typing typing = createTyping(map);
    CastCounter counter = new CastCounter(body, function, hierarchy);
    body = counter.insertCastStmts(typing);

    //Assert.assertTrue(counter.getCastCount() == 5);
    System.out.println(body);

  }

  private void setMethod(String methodName, Type returnType, List<Type> paras) {
    MethodSignature methodSignature =
        factory.getMethodSignature(classType, methodName, returnType, paras);
    Optional<JavaSootMethod> methodOptional = clazz.getMethod(methodSignature.getSubSignature());
    JavaSootMethod method = methodOptional.get();
    body = method.getBody();
  }

  private Typing createTyping(Map<String, Type> map) {
    Typing typing = new Typing(body.getLocals());
    for (Local l : typing.getLocals()) {
      // todo: [problem] body contains null local!!! (shift)
      if (l == null) {
        continue;
      }
      if (map.keySet().contains(l.getName())) {
        typing.set(l, map.get(l.getName()));
      }
    }
    return typing;
  }
}
