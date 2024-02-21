package sootup.java.bytecode.interceptors.typeresolving;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import categories.TestCategories;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.basic.Local;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ArrayType;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.java.bytecode.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.interceptors.TypeAssigner;
import sootup.java.bytecode.interceptors.typeresolving.types.TopType;
import sootup.java.core.JavaPackageName;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

@Tag(TestCategories.JAVA_8_CATEGORY)
public class TypeResolverTest extends TypeAssignerTestSuite {

  String baseDir = "../shared-test-resources/TypeResolverTestSuite/";

  @BeforeEach
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

    assertEquals(
        Stream.of(
                "CastCounterDemos l0",
                "Sub1 $stack4, l1",
                "Sub2 $stack5, l3",
                "byte l2",
                "l0 := @this: CastCounterDemos",
                "$stack4 = new Sub1",
                "specialinvoke $stack4.<Sub1: void <init>()>()",
                "l1 = $stack4",
                "l2 = 1",
                "$stack5 = new Sub2",
                "specialinvoke $stack5.<Sub2: void <init>()>()",
                "l3 = $stack5",
                "virtualinvoke l1.<Super1: void m(int,Sub2)>(l2, l3)",
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
    assertEquals(
        Stream.of(
                "CastCounterDemos l0",
                "Sub1 $stack3",
                "Super1 l2",
                "Super1[] l1",
                "l0 := @this: CastCounterDemos",
                "l1 = newarray (Super1)[10]",
                "$stack3 = new Sub1",
                "specialinvoke $stack3.<Sub1: void <init>()>()",
                "l1[0] = $stack3",
                "l2 = l1[2]",
                "return")
            .collect(Collectors.toList()),
        actualStmts);
  }

  @Test
  public void testArrayAssignStmt() {
    final JavaView view = new JavaView(new JavaClassPathAnalysisInputLocation(baseDir + "Misc/"));

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
    assertEquals(
        Stream.of(
                "int $stack3",
                "int[] l1",
                "java.io.PrintStream $stack4",
                "java.lang.Class $stack2",
                "java.lang.Object l0",
                "java.lang.String $stack5",
                "$stack2 = <java.lang.Integer: java.lang.Class TYPE>",
                "l0 = staticinvoke <java.lang.reflect.Array: java.lang.Object newInstance(java.lang.Class,int)>($stack2, 10)",
                "l1 = (int[]) l0",
                "$stack4 = <java.lang.System: java.io.PrintStream out>",
                "$stack3 = lengthof l1",
                "$stack5 = dynamicinvoke \"makeConcatWithConstants\" <java.lang.String (java.lang.Object,int)>(l0, $stack3) <java.lang.invoke.StringConcatFactory: java.lang.invoke.CallSite makeConcatWithConstants(java.lang.invoke.MethodHandles$Lookup,java.lang.String,java.lang.invoke.MethodType,java.lang.String,java.lang.Object[])>(\"\\u0001\\u0001\")",
                "virtualinvoke $stack4.<java.io.PrintStream: void println(java.lang.String)>($stack5)",
                "return")
            .collect(Collectors.toList()),
        actualStmts);

    final Optional<Local> any =
        builder.getLocals().stream().filter(l -> l.getName().equals("l1")).findAny();
    assertTrue(any.isPresent());
    assertEquals("int[]", any.get().getType().toString());
    assertEquals(ArrayType.class, any.get().getType().getClass());
  }

  @Test
  public void testFieldAssignment() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Application,
                Collections.singletonList(new TypeAssigner())));
    Body body =
        view.getMethod(
                view.getIdentifierFactory()
                    .getMethodSignature(
                        "FieldAssignment", "entry", "void", Collections.emptyList()))
            .get()
            .getBody();

    assertEquals(
        Stream.of(
                "FieldAssignment$A $stack2, l0",
                "java.lang.String l1",
                "$stack2 = new FieldAssignment$A",
                "specialinvoke $stack2.<FieldAssignment$A: void <init>()>()",
                "l0 = $stack2",
                "l1 = \"abc\"",
                // the assignment to a field of l0 doesn't change the type of l0
                "l0.<FieldAssignment$A: java.lang.String s> = l1",
                "return")
            .collect(Collectors.toList()),
        Utils.filterJimple(body.toString()));
  }

  @Test
  public void testArrayMixedAssignment() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "arraysMixedAssignment", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();

    // Tests that the augmented integer types (which are based on the value of integer constants)
    // don't change the type of `a`.
    Assert.assertEquals(ArrayType.createArrayType(PrimitiveType.getInt(), 1), arrayLocal.getType());
  }

  @Test
  public void testArrayAssignBeforeInit() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "arrayAssignBeforeInit", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();

    // Tests that assignments to an array index before the array is initialized (in the order of
    // source code/bytecode), results in the correct type.
    Assert.assertEquals(
        ArrayType.createArrayType(new JavaClassType("String", new JavaPackageName("java.lang")), 1),
        arrayLocal.getType());
  }

  @Test
  public void testNullArray() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "nullArray", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();

    // Tests that assignments to an array index before the array is initialized (in the order of
    // source code/bytecode), results in the correct type.
    Assert.assertEquals(
        ArrayType.createArrayType(new JavaClassType("Object", new JavaPackageName("java.lang")), 1),
        arrayLocal.getType());
  }

  @Test
  public void testObjectPrimitiveArray() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "objectPrimitiveArray", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();

    // Tests that an array that gets both objects and primitives assigned to it,
    // gets the `TopType[]` type.
    Assert.assertEquals(ArrayType.createArrayType(TopType.getInstance(), 1), arrayLocal.getType());
  }

  @Test
  public void testUseNullArray() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "useNullArray", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();
    Local objectLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l1")).findAny().get();

    // The type of `arrayLocal` should actually be `Object[]` to be more precise,
    // but that would require taking non-assignments into account for the typing.
    // The original paper doesn't do that, and it would only make a difference for this edge case.
    Assert.assertEquals(
        new JavaClassType("Object", new JavaPackageName("java.lang")), arrayLocal.getType());
    Assert.assertEquals(
        new JavaClassType("Object", new JavaPackageName("java.lang")), objectLocal.getType());
  }

  @Test
  public void testUsePrimitiveNullArray() {
    final JavaView view =
        new JavaView(
            new JavaClassPathAnalysisInputLocation(
                baseDir + "Misc/",
                SourceType.Library,
                Collections.singletonList(new TypeAssigner())));

    final MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("Misc", "usePrimitiveNullArray", "void", Collections.emptyList());
    final Body body = view.getMethod(methodSignature).get().getBody();

    Local arrayLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l0")).findAny().get();
    Local objectLocal =
        body.getLocals().stream().filter(local -> local.getName().equals("l1")).findAny().get();

    // Using a `null` array of primitive type, should not accidentally promote that array to an
    // array of references.
    Assert.assertEquals(ArrayType.createArrayType(PrimitiveType.getInt(), 1), arrayLocal.getType());
    Assert.assertEquals(PrimitiveType.getInt(), objectLocal.getType());
  }
}
