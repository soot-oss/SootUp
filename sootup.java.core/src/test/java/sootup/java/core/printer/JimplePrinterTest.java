package sootup.java.core.printer;

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.junit.Test;
import sootup.core.Project;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.OverridingClassSource;
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
import sootup.java.core.JavaIdentifierFactory;
import sootup.java.core.JavaProject;
import sootup.java.core.language.JavaLanguage;

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
    p.printTo(buildClass(), new PrintWriter(writer));

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

  private SootClass buildClass() {

    Project project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(new EagerInputLocation()).build();
    View view = project.createView();

    String className = "some.package.SomeClass";
    MethodSignature methodSignatureOne =
        view.getIdentifierFactory()
            .getMethodSignature("main", className, "void", Collections.emptyList());

    StmtPositionInfo noPosInfo = StmtPositionInfo.createNoStmtPositionInfo();
    final JReturnVoidStmt returnVoidStmt = new JReturnVoidStmt(noPosInfo);
    final JNopStmt jNop = new JNopStmt(noPosInfo);
    Body.BodyBuilder bodyBuilder = Body.builder();

    bodyBuilder
        .setStartingStmt(jNop)
        .addFlow(jNop, returnVoidStmt)
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
        view.getIdentifierFactory()
            .getMethodSignature("otherMethod", className, "int", Collections.emptyList());
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
                JavaIdentifierFactory.getInstance()
                    .getClassType("files.stuff.FileNotFoundException")),
            NoPositionInformation.getInstance());

    return new SootClass(
        new OverridingClassSource(
            new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            Collections.singleton(
                new SootField(
                    JavaIdentifierFactory.getInstance()
                        .getFieldSignature(
                            "counter",
                            JavaIdentifierFactory.getInstance().getClassType(className),
                            PrimitiveType.getInt()),
                    EnumSet.of(FieldModifier.PRIVATE),
                    NoPositionInformation.getInstance())),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.singleton(
                JavaIdentifierFactory.getInstance().getClassType("some.great.Interface")),
            JavaIdentifierFactory.getInstance().getClassType("some.great.Superclass"),
            null,
            NoPositionInformation.getInstance(),
            null,
            view.getIdentifierFactory().getClassType(className),
            new EagerInputLocation()),
        SourceType.Application);
  }
}
