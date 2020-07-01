package de.upb.swt.soot.test.core.printer;

import static org.junit.Assert.*;

import de.upb.swt.soot.core.Project;
import de.upb.swt.soot.core.frontend.OverridingClassSource;
import de.upb.swt.soot.core.frontend.OverridingMethodSource;
import de.upb.swt.soot.core.graph.MutableStmtGraph;
import de.upb.swt.soot.core.inputlocation.EagerInputLocation;
import de.upb.swt.soot.core.jimple.Jimple;
import de.upb.swt.soot.core.jimple.basic.NoPositionInformation;
import de.upb.swt.soot.core.jimple.basic.StmtPositionInfo;
import de.upb.swt.soot.core.jimple.common.stmt.JIdentityStmt;
import de.upb.swt.soot.core.jimple.common.stmt.JNopStmt;
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

/** @author Markus Schmidt */
public class PrinterTest {
  // import collisions are already testet in AbstractStmtPrinterTest covered in
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
            "private int otherMethod() throws FileNotFoundException",
            "nop"),
        Utils.filterJimple(writer.toString()));
  }

  private SootClass buildClass() {

    Project project =
        JavaProject.builder(new JavaLanguage(8)).addClassPath(new EagerInputLocation()).build();
    View view = project.createOnDemandView();

    final MutableStmtGraph graph = new MutableStmtGraph();
    graph.addNode(new JNopStmt(StmtPositionInfo.createNoStmtPositionInfo()));

    final MethodSignature methodSig =
        JavaIdentifierFactory.getInstance()
            .getMethodSignature("test", "foo", "int", Collections.emptyList());

    String className = "some.package.SomeClass";
    MethodSignature methodSignatureOne =
        view.getIdentifierFactory()
            .getMethodSignature("main", className, "void", Collections.emptyList());

      Body.BodyBuilder bodyBuilder = Body.builder();

      JNopStmt firstStmt = Jimple.newNopStmt(StmtPositionInfo.createNoStmtPositionInfo());
      bodyBuilder.addStmt(
              firstStmt);
      bodyBuilder
              .setMethodSignature(methodSignatureOne)
              .setLocals(Collections.emptySet())
              .setTraps(Collections.emptyList())
              .setFirstStmt(firstStmt)
              .setPosition(null);
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
              .setLocals(Collections.emptySet())
              .setTraps(Collections.emptyList())
              .setFirstStmt(firstStmt)
              .setPosition(null);
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
            new HashSet<>(Arrays.asList(dummyMainMethod, anotherMethod)),
            NoPositionInformation.getInstance(),
            EnumSet.of(Modifier.PUBLIC)),
        SourceType.Application);
  }
}
