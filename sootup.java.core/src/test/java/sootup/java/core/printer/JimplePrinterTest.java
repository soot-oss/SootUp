package sootup.java.core.printer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.core.util.printer.JimplePrinter;
import sootup.core.views.View;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

/**
 * @author Markus Schmidt
 * @author Kaustubh Kelkar updated on 02.07.2020
 */
public class JimplePrinterTest {
  // import collisions are already tested in AbstractStmtPrinterTest covered in
  // AbstractStmtPrinterTest

  @Test
  public void testPrintedExample() {

    JimplePrinter p =
        new JimplePrinter(JimplePrinter.Option.Deterministic, JimplePrinter.Option.UseImports);
    final StringWriter writer = new StringWriter();
    JavaSootClass sootClass = buildClass(false, false);
    p.printTo(sootClass, new PrintWriter(writer));

    assertEquals(
        Arrays.asList(
            "import files.stuff.FileNotFoundException",
            "import some.great.Interface",
            "public class SomeClass extends Superclass implements Interface",
            "private int counter",
            "private int otherMethod() throws FileNotFoundException",
            "nop",
            "return",
            "public static void main()",
            "nop",
            "return"),
        Utils.filterJimple(writer.toString()));
  }

  @Test
  public void testSootClassBuilder() {
    JimplePrinter p =
        new JimplePrinter(JimplePrinter.Option.UseImports, JimplePrinter.Option.Deterministic);
    final StringWriter writer = new StringWriter();
    final StringWriter writer1 = new StringWriter();
    final StringWriter writer2 = new StringWriter();
    SootClass sootClass = buildClass(false, false);
    JavaSootClass sootClassUsingBuilder = buildClass(true, false);
    JavaSootClass sootClassUsingBuilderWithMembers = buildClass(true, true);
    p.printTo(sootClass, new PrintWriter(writer));
    p.printTo(sootClassUsingBuilder, new PrintWriter(writer1));
    p.printTo(sootClassUsingBuilderWithMembers, new PrintWriter(writer2));
    assertEquals(Utils.filterJimple(writer.toString()), Utils.filterJimple(writer1.toString()));
    assertEquals(Utils.filterJimple(writer1.toString()), Utils.filterJimple(writer2.toString()));

    // assert if sootClass and sootClassUsingBuilder are same
    assertEquals(
        sootClass.getClassSource().getClassType().getClassName(),
        sootClassUsingBuilder.getClassSource().getClassType().getClassName());
    assertEquals(sootClass.getMethods().size(), sootClassUsingBuilder.getMethods().size());
    assertEquals(sootClass.getFields().size(), sootClassUsingBuilder.getFields().size());
    assertEquals(sootClass.getModifiers().size(), sootClassUsingBuilder.getModifiers().size());
    assertEquals(sootClass.getInterfaces().size(), sootClassUsingBuilder.getInterfaces().size());
    assertEquals(
        sootClass.getSuperclass().get().getClassName(),
        sootClassUsingBuilder.getSuperclass().get().getClassName());
  }

  private JavaSootClass buildClass(boolean buildUsingBuilder, boolean buildWithClassMembers) {
    View view = new JavaView(new EagerInputLocation());

    String className = "some.package.SomeClass";
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    MethodSignature methodSignatureOne =
        identifierFactory.getMethodSignature(className, "main", "void", Collections.emptyList());

    StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();
    final JReturnVoidStmt returnVoidStmt = new JReturnVoidStmt(noPosInfo);
    final JNopStmt jNop = new JNopStmt(noPosInfo);
    Body.BodyBuilder bodyBuilder = Body.builder();

    MutableControlFlowGraph controlFlowGraph = bodyBuilder.getControlFlowGraph();
    controlFlowGraph.setStartingStmt(jNop);
    controlFlowGraph.putEdge(jNop, returnVoidStmt);

    bodyBuilder
        .setMethodSignature(methodSignatureOne)
        .setPosition(NoPositionInformation.getInstance());
    Body bodyOne = bodyBuilder.build();

    JavaSootMethod dummyMainMethod;
    if (buildUsingBuilder) {
      dummyMainMethod =
          JavaSootMethod.JavaSootMethodBuilder.builder()
              .withSource(new OverridingBodySource(methodSignatureOne, bodyOne))
              .withSignature(methodSignatureOne)
              .withModifier(EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC))
              .withAnnotation(Collections.emptyList())
              .withPosition(NoPositionInformation.getInstance())
              .build();
    } else {
      dummyMainMethod =
          new JavaSootMethod(
              new OverridingBodySource(methodSignatureOne, bodyOne),
              methodSignatureOne,
              EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
              Collections.emptyList(),
              NoPositionInformation.getInstance());
    }

    MethodSignature methodSignatureTwo =
        identifierFactory.getMethodSignature(
            className, "otherMethod", "int", Collections.emptyList());
    bodyBuilder
        .setMethodSignature(methodSignatureTwo)
        .setPosition(NoPositionInformation.getInstance());
    Body bodyTwo = bodyBuilder.build();

    JavaSootMethod anotherMethod;
    if (buildUsingBuilder) {
      anotherMethod =
          JavaSootMethod.JavaSootMethodBuilder.builder()
              .withSource(new OverridingBodySource(methodSignatureOne, bodyTwo))
              .withSignature(methodSignatureTwo)
              .withModifiers(MethodModifier.PRIVATE)
              .withThrownExceptions(
                  Collections.singletonList(
                      identifierFactory.getClassType("files.stuff.FileNotFoundException")))
              .withPosition(NoPositionInformation.getInstance())
              .build();
    } else {
      anotherMethod =
          new JavaSootMethod(
              new OverridingBodySource(methodSignatureOne, bodyTwo),
              methodSignatureTwo,
              EnumSet.of(MethodModifier.PRIVATE),
              Collections.singletonList(
                  identifierFactory.getClassType("files.stuff.FileNotFoundException")),
              NoPositionInformation.getInstance());
    }

    if (buildUsingBuilder) {
      return getSootClassUsingBuilder(
          dummyMainMethod, anotherMethod, className, view, buildWithClassMembers);
    }

    return getSootClass(dummyMainMethod, anotherMethod, className, view);
  }

  private JavaSootClass getSootClassUsingBuilder(
      JavaSootMethod dummyMainMethod,
      JavaSootMethod anotherMethod,
      String className,
      View view,
      boolean buildWithClassMembers) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaSootField sootField;
    FieldSignature fieldSignature =
        identifierFactory.getFieldSignature(
            "counter", identifierFactory.getClassType(className), PrimitiveType.getInt());
    if (buildWithClassMembers) {
      sootField =
          JavaSootField.JavaSootFieldBuilder.builder()
              .withSignature(fieldSignature)
              .withModifiers(FieldModifier.PRIVATE)
              .withPosition(NoPositionInformation.getInstance())
              .build();
    } else {
      sootField =
          new JavaSootField(
              fieldSignature,
              EnumSet.of(FieldModifier.PRIVATE),
              NoPositionInformation.getInstance());
    }

    InMemoryOverridingJavaClassSource overridingClassSource =
        new InMemoryOverridingJavaClassSource(
            new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            Collections.singleton(sootField),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.singleton(
                (JavaClassType) identifierFactory.getClassType("some.great.Interface")),
            (JavaClassType) identifierFactory.getClassType("some.great.Superclass"),
            null,
            NoPositionInformation.getInstance(),
            null,
            identifierFactory.getClassType(className),
            new EagerInputLocation());

    if (buildWithClassMembers) {
      return JavaSootClass.JavaSootClassBuilder.builder()
          .withClassSource(overridingClassSource)
          .withSourceType(SourceType.Application)
          .withMethods(new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)))
          .withField(sootField)
          .withModifiers(EnumSet.of(ClassModifier.PUBLIC))
          .withInterfaces(
              Collections.singleton(identifierFactory.getClassType("some.great.Interface")))
          .withSuperclass(Optional.of(identifierFactory.getClassType("some.great.Superclass")))
          .withPosition(NoPositionInformation.getInstance())
          .withClassType(identifierFactory.getClassType(className))
          .build();
    }

    return JavaSootClass.JavaSootClassBuilder.builder()
        .withClassSource(overridingClassSource)
        .withSourceType(SourceType.Application)
        .build();
  }

  private JavaSootClass getSootClass(
      JavaSootMethod dummyMainMethod, JavaSootMethod anotherMethod, String className, View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    JavaSootField sootField =
        new JavaSootField(
            identifierFactory.getFieldSignature(
                "counter", identifierFactory.getClassType(className), PrimitiveType.getInt()),
            EnumSet.of(FieldModifier.PRIVATE),
            NoPositionInformation.getInstance());
    InMemoryOverridingJavaClassSource overridingClassSource =
        new InMemoryOverridingJavaClassSource(
            new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            Collections.singleton(sootField),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.singleton(
                (JavaClassType) identifierFactory.getClassType("some.great.Interface")),
            (JavaClassType) identifierFactory.getClassType("some.great.Superclass"),
            null,
            NoPositionInformation.getInstance(),
            null,
            identifierFactory.getClassType(className),
            new EagerInputLocation());
    return new JavaSootClass(overridingClassSource, SourceType.Application);
  }
}
