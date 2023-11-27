package sootup.java.bytecode.interceptors.typeresolving;

import categories.Java8Test;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;
import sootup.java.core.views.JavaView;

@Category(Java8Test.class)
public class TypeResolverTest extends TypeAssignerTestSuite {

  String baseDir = "../shared-test-resources/TypeResolverTestSuite/";

  @Before
  public void setup() {
    String className = "CastCounterDemos";
    buildView(baseDir + "CastCounterTest/", className);
  }

  @Test
  public void testInvokeStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("invokeStmt", "void");
    TypeResolver resolver = new TypeResolver(view);
    resolver.resolve(builder);
    Body newbody = builder.build();

    List<String> actualStmts = Utils.filterJimple(newbody.toString());

    Assert.assertEquals(
        Stream.of(
                "CastCounterDemos $l0",
                "Sub1 $l1, $stack4",
                "Sub2 $l3, $stack5",
                "byte $l2",
                "$l0 := @this: CastCounterDemos",
                "$stack4 = new Sub1",
                "specialinvoke $stack4.<Sub1: void <init>()>()",
                "$l1 = $stack4",
                "$l2 = 1",
                "$stack5 = new Sub2",
                "specialinvoke $stack5.<Sub2: void <init>()>()",
                "$l3 = $stack5",
                "virtualinvoke $l1.<Super1: void m(int,Sub2)>($l2, $l3)",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }

  @Test
  public void testAssignStmt() {
    final Body.BodyBuilder builder = createMethodsBuilder("assignStmt", "void");

    TypeResolver resolver = new TypeResolver(view);
    resolver.resolve(builder);
    Body newbody = builder.build();
    List<String> actualStmts = Utils.filterJimple(newbody.toString());
    Assert.assertEquals(
        Stream.of(
                "CastCounterDemos $l0",
                "Sub1 $stack3",
                "Super1 $l2",
                "Super1[] $l1",
                "$l0 := @this: CastCounterDemos",
                "$l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "$l1[0] = $stack3",
                "$l2 = $l1[2]",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }

  @Test
  public void testArrayAssignStmt() {

    final JavaProject project =
        JavaProject.builder(new JavaLanguage(8))
            .addInputLocation(new JavaClassPathAnalysisInputLocation(baseDir + "Misc/"))
            .build();
    final JavaView view = project.createView();

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("NewArrayInstance", "entry", "void", Collections.emptyList());
    final Optional<? extends SootMethod> methodOpt = view.getMethod(methodSignature);
    final SootMethod sootMethod = methodOpt.get();
    final Body.BodyBuilder builder =
        Body.builder(sootMethod.getBody(), EnumSet.noneOf(MethodModifier.class));

    TypeResolver resolver = new TypeResolver(view);
    resolver.resolve(builder);
    Body newbody = builder.build();
    List<String> actualStmts = Utils.filterJimple(newbody.toString());
    Assert.assertEquals(
        Stream.of(
                "int $stack3",
                "int[] $l1",
                "java.io.PrintStream $stack4",
                "java.lang.Class $stack2",
                "java.lang.Object $l0",
                "java.lang.String $stack5",
                "$stack2 = <java.lang.Integer: java.lang.Class TYPE>",
                "$l0 = staticinvoke <java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>($stack2, 10)",
                "$l1 = (int[]) $l0",
                "$stack4 = <java.lang.System: java.io.PrintStream out>",
                "$stack3 = lengthof $l1",
                "$stack5 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.Object,int)>($l0, $stack3) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001\\u0001\")",
                "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
                "return")
            .collect(Collectors.toList()),
        actualStmts);

    final Optional<Local> any =
        builder.getLocals().stream().filter(l -> l.getName().equals("$l1")).findAny();
    Assert.assertTrue(any.isPresent());
    Assert.assertEquals("int[]", any.get().getType().toString());
    Assert.assertEquals(ArrayType.class, any.get().getType().getClass());
  }
}
