package referencejimple;

import com.ibm.wala.cast.loader.AstMethod;
import de.upb.soot.core.Body;
import de.upb.soot.core.ClassType;
import de.upb.soot.core.Modifier;
import de.upb.soot.core.ResolvingLevel;
import de.upb.soot.core.SootClass;
import de.upb.soot.core.SootField;
import de.upb.soot.core.SootMethod;
import de.upb.soot.frontends.IMethodSourceContent;
import de.upb.soot.frontends.JavaClassSource;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.Local;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.NoPositionInformation;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.basic.Trap;
import de.upb.soot.jimple.basic.Value;
import de.upb.soot.jimple.common.constant.IntConstant;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.IntType;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.VoidType;
import de.upb.soot.namespaces.JavaClassPathNamespace;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.FieldSignature;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.signatures.TypeSignature;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/** @author Markus Schmidt */
class DummyMethodSource implements IMethodSourceContent {
  private Body body;
  private MethodSignature methodSignature;

  public DummyMethodSource(MethodSignature methodSignature, Body body) {
    this.body = body;
    this.methodSignature = methodSignature;
  }

  @Override
  public Body getBody(SootMethod m) {
    return body;
  }

  @Override
  public MethodSignature getSignature() {
    return methodSignature;
  }
}

public class IdentityStmtTest extends JimpleInstructionsTestBase {

  SootField initField, declField;
  JavaClassSignature classSignature;

  @Override
  public void build() {

    DefaultSignatureFactory dsm = new DefaultSignatureFactory();

    Path dummyPath = Paths.get(URI.create("file:/C:/nonexistent.java"));
    JavaClassSource javaClassSource =
        new JavaClassSource(
            new JavaClassPathNamespace("src/main/java/de/upb/soot"),
            dummyPath,
            dsm.getClassSignature("de.upb.soot.instructions.stmt.IdentityStmt"));

    JavaClassSignature superClassSignature = dsm.getClassSignature("java.lang.Object");
    classSignature = dsm.getClassSignature("de.upb.soot.instructions.stmt.IdentityStmt");

    Set<SootField> fields = new LinkedHashSet<SootField>();

    declField =
        new SootField(
            view,
            classSignature,
            dsm.getFieldSignature("declProperty", classSignature, IntType.INSTANCE.toString()),
            dsm.getTypeSignature(IntType.INSTANCE.toString()));
    fields.add(declField);
    initField =
        new SootField(
            view,
            classSignature,
            dsm.getFieldSignature("initProperty", classSignature, IntType.INSTANCE.toString()),
            dsm.getTypeSignature(IntType.INSTANCE.toString()));
    fields.add(initField);

    Set<SootMethod> methods = new LinkedHashSet<>();

    methods.add(init());

    // methods.add( atThis());
    // atParameterPrimitive();
    // atParameterNonPrimitive();
    // atExceptionThrow();
    // atExceptionThrowAndCatch();

    sootClass =
        new SootClass(
            view,
            ResolvingLevel.BODIES,
            javaClassSource,
            ClassType.Application,
            superClassSignature,
            new HashSet<>(),
            null,
            fields,
            methods,
            new NoPositionInformation(),
            EnumSet.of(Modifier.PUBLIC));
  }

  SootMethod init() {
    PositionInfo nop = PositionInfo.createNoPositionInfo();
    DefaultSignatureFactory dsm = new DefaultSignatureFactory();
    LocalGenerator generator = new LocalGenerator();

    MethodSignature methodSignature =
        dsm.getMethodSignature(
            "<init>", classSignature, VoidType.INSTANCE.toString(), Arrays.asList(""));
    AstMethod.DebuggingInformation debugInfo = null;

    List<Local> locals = new LinkedList<>();
    List<Trap> traps = new LinkedList<>();
    List<IStmt> stmts = new LinkedList<>();

    new RefType(view, dsm.getTypeSignature("de.upb.soot.instructions.stmt.IdentityStmt"));
    RefType type = RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt");

    Local r0 = generator.generateField(type);
    stmts.add(Jimple.newIdentityStmt(r0, Jimple.newThisRef(type), nop));

    // TODO: how to add expr to body?
    // add(Jimple.newSpecialInvokeExpr( r0 , currentMethod ));

    Value value = IntConstant.getInstance(42);
    stmts.add(
        Jimple.newAssignStmt(
            Jimple.newInstanceFieldRef(view, r0, (FieldSignature) initField.getSignature()),
            value,
            nop));

    stmts.add(Jimple.newReturnVoidStmt(nop));

    Body body = new Body(locals, traps, stmts, new NoPositionInformation());
    IMethodSourceContent methodSource = new DummyMethodSource(methodSignature, body);
    SootMethod currentMethod =
        new SootMethod(
            view,
            classSignature,
            methodSource,
            Arrays.asList(new TypeSignature[] {}),
            dsm.getTypeSignature("void"),
            EnumSet.of(Modifier.PUBLIC),
            debugInfo);

    return currentMethod;
  }

  /*
   *
   * SootMethod atThis(){
   *
   * DefaultSignatureFactory dsm = new DefaultSignatureFactory();
   *
   * SootMethod currentMethod = new SootMethod(view, "atThis", Arrays.asList(new Type[]{}), VoidType.INSTANCE,
   * EnumSet.of(Modifier.PUBLIC) ); sootClass.addMethod(currentMethod);
   *
   * SootMethod println = new SootMethod(view, "println", Arrays.asList(new Type[]{ IntType.INSTANCE} ), VoidType.INSTANCE,
   * EnumSet.of(Modifier.PUBLIC, Modifier.STATIC) ); println.setDeclaringClass( new SootClass(view,
   * dsm.getClassSignature("java.lang.System") , EnumSet.of(Modifier.PUBLIC, Modifier.FINAL)) );
   *
   * Body body = Jimple.newBody(currentMethod); LocalGenerator generator = new LocalGenerator(body);
   *
   * //view.addRefType(new RefType(view, "de.upb.soot.instructions.stmt.IdentityStmt")); RefType type =
   * RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt"); // view.addRefType(new RefType(view,
   * "java.io.PrintStream")); RefType printStream = RefType.getInstance("java.io.PrintStream"); // view.addRefType(new
   * RefType(view, "java.lang.System")); RefType system = RefType.getInstance("java.lang.System");
   *
   *
   * Local r0 = generator.generateField( type ); body.addStmt(Jimple.newIdentityStmt( r0 , Jimple.newThisRef(type) ));
   *
   * Local r1 = generator.generateLocal( printStream); SootField out = new SootField(view, "out", system,
   * EnumSet.<Modifier>of(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL) );
   *
   *
   * // TODO: expr to body // Jimple.newAssignStmt( r1, Jimple.newStaticField(view, out ) ); // Jimple.newAssignStmt( r1,
   * Jimple.newInstanceFieldRef(r1,Jimple.newStaticField(view, out )) );
   *
   *
   *
   * Local i0 = generator.generateLocal( IntType.INSTANCE); Value value = IntConstant.getInstance( 42 );
   * body.addStmt(Jimple.newAssignStmt( i0, Jimple.newInstanceFieldRef( r0 , declField ) )); // TODO: add to body //
   * Jimple.newVirtualInvokeExpr(r1, println, i0);
   *
   *
   * Local i1 = generator.generateLocal( IntType.INSTANCE); Local i2 = generator.generateLocal( IntType.INSTANCE); Local i3 =
   * generator.generateLocal( IntType.INSTANCE);
   *
   * Local r2 = generator.generateLocal( printStream); Local r3 = generator.generateLocal( printStream); Local r4 =
   * generator.generateLocal( printStream);
   *
   *
   *
   * body.addStmt(Jimple.newReturnVoidStmt() ); currentMethod.setActiveBody(body);
   *
   * }
   *
   * SootMethod atParameterPrimitive(){
   *
   * SootMethod currentMethod = new SootMethod(view, "atParameterPrimitive", Arrays.asList(new Type[]{IntType.INSTANCE,
   * BooleanType.INSTANCE}), VoidType.INSTANCE, EnumSet.of(Modifier.PUBLIC) ); sootClass.addMethod(currentMethod);
   *
   * Body body = Jimple.newBody(currentMethod); LocalGenerator generator = new LocalGenerator(body);
   *
   * RefType type = RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt");
   *
   * Local r0 = generator.generateField( type ); body.addStmt(Jimple.newIdentityStmt( r0 , Jimple.newThisRef(type) ));
   *
   * Local i0 = generator.generateField( IntType.INSTANCE); body.addStmt( Jimple.newIdentityStmt( i0, Jimple.newParameterRef(
   * IntType.INSTANCE, 0)) );
   *
   * Local z0 = generator.generateField( BooleanType.INSTANCE); body.addStmt( Jimple.newIdentityStmt( z0,
   * Jimple.newParameterRef( BooleanType.INSTANCE, 1)) );
   *
   *
   * type = RefType.getInstance("java.io.PrintStream"); Local r1 = generator.generateLocal( type ); Local r2 =
   * generator.generateLocal( type );
   *
   *
   *
   *
   * body.addStmt(Jimple.newReturnVoidStmt() ); currentMethod.setActiveBody(body);
   *
   *
   *
   * }
   *
   * SootMethod atParameterNonPrimitive(){
   *
   * SootMethod currentMethod = new SootMethod(view, "atParameterNonPrimitive", Arrays.asList(new Type[]{
   * RefType.getInstance("java.lang.Integer"), RefType.getInstance("java.lang.String"),
   * RefType.getInstance("java.lang.Boolean"), RefType.getInstance("int[]")
   *
   * }), VoidType.INSTANCE, EnumSet.of(Modifier.PUBLIC) ); sootClass.addMethod(currentMethod);
   *
   * Body body = Jimple.newBody(currentMethod); LocalGenerator generator = new LocalGenerator(body);
   *
   * RefType type = RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt"); Local r0 = generator.generateField(
   * type ); body.addStmt(Jimple.newIdentityStmt( r0 , Jimple.newThisRef(type) ));
   *
   * type = RefType.getInstance("java.lang.Integer"); Local r1= generator.generateField( type ); body.addStmt(
   * Jimple.newIdentityStmt( r1, Jimple.newParameterRef( type, 0)) );
   *
   * type = RefType.getInstance("java.lang.String"); Local r2 = generator.generateField( type ); body.addStmt(
   * Jimple.newIdentityStmt( r2, Jimple.newParameterRef( type, 1)) );
   *
   * type = RefType.getInstance("java.lang.Boolean"); Local r3 = generator.generateField( type ); body.addStmt(
   * Jimple.newIdentityStmt( r3, Jimple.newParameterRef( type, 2)) );
   *
   * type = RefType.getInstance("int[]"); Local r4 = generator.generateField( type ); body.addStmt( Jimple.newIdentityStmt(
   * r4, Jimple.newParameterRef( type, 3)) );
   *
   *
   *
   *
   *
   * type = RefType.getInstance("java.io.PrintStream"); Local r5 = generator.generateLocal( type ); Local r6 =
   * generator.generateLocal( type ); Local r7 = generator.generateLocal( type ); Local r8 = generator.generateLocal( type );
   *
   *
   * body.addStmt(Jimple.newReturnVoidStmt() ); currentMethod.setActiveBody(body);
   *
   * }
   *
   * SootMethod atExceptionThrow(){
   *
   * SootClass exception = new SootClass(view, new DefaultSignatureFactory().getClassSignature("java.lang.Exception"));
   * SootMethod currentMethod = new SootMethod(view, "atExceptionThrow", Arrays.asList(new Type[]{}), VoidType.INSTANCE,
   * EnumSet.of(Modifier.PUBLIC) , Arrays.asList(exception) ); sootClass.addMethod(currentMethod);
   *
   * Body body = Jimple.newBody(currentMethod); LocalGenerator generator = new LocalGenerator(body);
   *
   * RefType type = RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt"); Local r0 = generator.generateField(
   * type ); body.addStmt(Jimple.newIdentityStmt( r0 , Jimple.newThisRef(type) ));
   *
   * type = RefType.getInstance("java.lang.Exception"); Local r1 = generator.generateLocal( type );
   * body.addStmt(Jimple.newAssignStmt( r1, Jimple.newNewExpr( type)) );
   *
   * // TODO: // specialinvoke $r1.<java.lang.Exception: void <init>(java.lang.String)>("Issue");
   *
   * body.addStmt(Jimple.newThrowStmt(r1) ); body.addStmt(Jimple.newReturnVoidStmt() ); currentMethod.setActiveBody(body);
   *
   *
   * }
   *
   * SootMethod atExceptionThrowAndCatch() {
   *
   * SootMethod currentMethod = new SootMethod(view, "atExceptionThrowAndCatch", Arrays.asList(new Type[]{}),
   * VoidType.INSTANCE, EnumSet.of(Modifier.PUBLIC) ); sootClass.addMethod(currentMethod);
   *
   * Body body = Jimple.newBody(currentMethod); LocalGenerator generator = new LocalGenerator(body);
   *
   * RefType type = RefType.getInstance("de.upb.soot.instructions.stmt.IdentityStmt"); Local r0 = generator.generateField(
   * type ); body.addStmt(Jimple.newIdentityStmt( r0 , Jimple.newThisRef(type) ));
   *
   * type = RefType.getInstance("java.io.PrintStream"); Local r1 = generator.generateLocal( type ); Local r2 =
   * generator.generateLocal( type ); Local r3 = generator.generateLocal( type );
   *
   * Local i0 = generator.generateLocal( IntType.INSTANCE ); Local i1 = generator.generateLocal( IntType.INSTANCE ); Local i2
   * = generator.generateField( IntType.INSTANCE );
   *
   * type = RefType.getInstance("java.lang.Exception"); Local r4 = generator.generateLocal( type );
   * body.addStmt(Jimple.newAssignStmt( r4, Jimple.newNewExpr( type)) );
   *
   * Local r5 = generator.generateLocal( RefType.getInstance("java.io.PrintStream") );
   *
   *
   * /* TODO label1: $r1 = <java.lang.System: java.io.PrintStream out>; virtualinvoke $r1.<java.io.PrintStream: void
   * println(java.lang.String)>("A1"); $i1 = r0.<de.upb.soot.instructions.stmt.IdentityStmt: int declProperty>; $i0 =
   * r0.<de.upb.soot.instructions.stmt.IdentityStmt: int initProperty>; i2 = $i1 * $i0; $r2 = <java.lang.System:
   * java.io.PrintStream out>; virtualinvoke $r2.<java.io.PrintStream: void println(int)>(i2);
   *
   * ...
   *
   * /
   *
   *
   *
   *
   *
   * // body.addStmt(Jimple.newThrowStmt(r1) ); body.addStmt(Jimple.newReturnVoidStmt() ); currentMethod.setActiveBody(body);
   *
   * }
   *
   * SootMethod exceptionMultiple(){
   *
   * }
   *
   * SootMethod exceptionFinally(){
   *
   *
   * }
   *
   */
}
