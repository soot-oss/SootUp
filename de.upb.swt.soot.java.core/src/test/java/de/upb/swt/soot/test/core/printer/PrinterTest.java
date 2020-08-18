package de.upb.swt.soot.test.core.printer;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.swt.soot.core.model.*;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.util.Utils;
import de.upb.swt.soot.core.util.printer.Printer;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.JavaProject;
import de.upb.swt.soot.java.core.language.JavaLanguage;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.junit.Test;

/**
 * @author Markus Schmidt
 * @author Kaustubh Kelkar updated on 02.07.2020
 */
public class PrinterTest {
  // import collisions are already tested in AbstractStmtPrinterTest covered in
  // AbstractStmtPrinterTest

  @Test
  public void testPrintedExample() {

    Printer p = new Printer(Printer.Option.UseImports);
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
        JavaProject.builder(new JavaLanguage(8)).addClassPath(new EagerInputLocation()).build();
    View view = project.createOnDemandView();

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
            new OverridingMethodSource(methodSignatureOne, bodyOne),
            methodSignatureOne,
            EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
            Collections.emptyList());

    MethodSignature methodSignatureTwo =
        view.getIdentifierFactory()
            .getMethodSignature("otherMethod", className, "int", Collections.emptyList());
    bodyBuilder
        .setMethodSignature(methodSignatureTwo)
        .setPosition(NoPositionInformation.getInstance());
    Body bodyTwo = bodyBuilder.build();

    SootMethod anotherMethod =
        new SootMethod(
            new OverridingMethodSource(methodSignatureOne, bodyTwo),
            methodSignatureTwo,
            EnumSet.of(Modifier.PRIVATE),
            Collections.singletonList(
                JavaIdentifierFactory.getInstance()
                    .getClassType("files.stuff.FileNotFoundException")));

    return new SootClass(
        new OverridingClassSource(
            new EagerInputLocation(),
            null,
            view.getIdentifierFactory().getClassType(className),
            JavaIdentifierFactory.getInstance().getClassType("some.great.Superclass"),
            Collections.singleton(
                JavaIdentifierFactory.getInstance().getClassType("some.great.Interface")),
            null,
            Collections.singleton(
                new SootField(
                    JavaIdentifierFactory.getInstance()
                        .getFieldSignature(
                            "counter",
                            JavaIdentifierFactory.getInstance().getClassType(className),
                            PrimitiveType.getInt()),
                    EnumSet.of(Modifier.PRIVATE))),
            new LinkedHashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            NoPositionInformation.getInstance(),
            EnumSet.of(Modifier.PUBLIC)),
        SourceType.Application);
  }
}
