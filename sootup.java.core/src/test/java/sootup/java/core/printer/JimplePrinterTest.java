package sootup.java.core.printer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.OverridingClassSource;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.PrimitiveType;
import sootup.core.util.Utils;
import sootup.core.util.printer.JimplePrinter;
import sootup.core.views.View;
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

    JimplePrinter p = new JimplePrinter(JimplePrinter.Option.UseImports);
    final StringWriter writer = new StringWriter();
    SootClass sootClass = buildClass(false);
    p.printTo(sootClass, new PrintWriter(writer));

    assertEquals(
        Arrays.asList(
            "import files.stuff.FileNotFoundException",
            "import some.great.Interface",
            "public class SomeClass extends Superclass implements Interface",
            "private int counter",
            "public static void main()",
            "nop",
            "return",
            "private int otherMethod() throws FileNotFoundException",
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
    SootClass sootClass = buildClass(false);
    SootClass sootClassUsingBuilder = buildClass(true);
    p.printTo(sootClass, new PrintWriter(writer));
    p.printTo(sootClassUsingBuilder, new PrintWriter(writer1));
    assertEquals(Utils.filterJimple(writer.toString()), Utils.filterJimple(writer1.toString()));

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

  private SootClass buildClass(boolean buildUsingBuilder) {
    View view = new JavaView(new EagerInputLocation());

    String className = "some.package.SomeClass";
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    MethodSignature methodSignatureOne =
        identifierFactory.getMethodSignature(className, "main", "void", Collections.emptyList());

    StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();
    final JReturnVoidStmt returnVoidStmt = new JReturnVoidStmt(noPosInfo);
    final JNopStmt jNop = new JNopStmt(noPosInfo);
    Body.BodyBuilder bodyBuilder = Body.builder();

    MutableStmtGraph stmtGraph = bodyBuilder.getStmtGraph();
    stmtGraph.setStartingStmt(jNop);
    stmtGraph.putEdge(jNop, returnVoidStmt);

    bodyBuilder
        .setMethodSignature(methodSignatureOne)
        .setPosition(NoPositionInformation.getInstance());
    Body bodyOne = bodyBuilder.build();

    SootMethod dummyMainMethod =
        new SootMethod(
            new OverridingBodySource(methodSignatureOne, bodyOne),
            methodSignatureOne,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    MethodSignature methodSignatureTwo =
        identifierFactory.getMethodSignature(
            className, "otherMethod", "int", Collections.emptyList());
    bodyBuilder
        .setMethodSignature(methodSignatureTwo)
        .setPosition(NoPositionInformation.getInstance());
    Body bodyTwo = bodyBuilder.build();

    SootMethod anotherMethod =
        new SootMethod(
            new OverridingBodySource(methodSignatureOne, bodyTwo),
            methodSignatureTwo,
            EnumSet.of(MethodModifier.PRIVATE),
            Collections.singletonList(
                identifierFactory.getClassType("files.stuff.FileNotFoundException")),
            NoPositionInformation.getInstance());

    if (buildUsingBuilder) {
      return getSootClassUsingBuilder(dummyMainMethod, anotherMethod, className, view);
    }

    return getSootClass(dummyMainMethod, anotherMethod, className, view);
  }

  private SootClass getSootClassUsingBuilder(
      SootMethod dummyMainMethod, SootMethod anotherMethod, String className, View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    SootField sootField =
        new SootField(
            identifierFactory.getFieldSignature(
                "counter", identifierFactory.getClassType(className), PrimitiveType.getInt()),
            EnumSet.of(FieldModifier.PRIVATE),
            NoPositionInformation.getInstance());

    OverridingClassSource overridingClassSource =
        OverridingClassSource.OverridingClassSourceBuilder.builder()
            .withMethods(new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)))
            .withField(sootField)
            .withModifiers(EnumSet.of(ClassModifier.PUBLIC))
            .withInterfaces(
                Collections.singleton(identifierFactory.getClassType("some.great.Interface")))
            .withSuperclass(Optional.of(identifierFactory.getClassType("some.great.Superclass")))
            .withPosition(NoPositionInformation.getInstance())
            .withClassType(identifierFactory.getClassType(className))
            .withAnalysisInputLocation(new EagerInputLocation())
            .build();

    SootClass sootClass =
        SootClass.SootClassBuilder.builder()
            .withClassSource(overridingClassSource)
            .withSourceType(SourceType.Application)
            .build();
    return sootClass;
  }

  private SootClass getSootClass(
      SootMethod dummyMainMethod, SootMethod anotherMethod, String className, View view) {
    IdentifierFactory identifierFactory = view.getIdentifierFactory();
    SootField sootField =
        new SootField(
            identifierFactory.getFieldSignature(
                "counter", identifierFactory.getClassType(className), PrimitiveType.getInt()),
            EnumSet.of(FieldModifier.PRIVATE),
            NoPositionInformation.getInstance());
    OverridingClassSource overridingClassSource =
        new OverridingClassSource(
            new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            Collections.singleton(sootField),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.singleton(identifierFactory.getClassType("some.great.Interface")),
            identifierFactory.getClassType("some.great.Superclass"),
            null,
            NoPositionInformation.getInstance(),
            null,
            identifierFactory.getClassType(className),
            new EagerInputLocation());
    return new SootClass(overridingClassSource, SourceType.Application);
  }
}
