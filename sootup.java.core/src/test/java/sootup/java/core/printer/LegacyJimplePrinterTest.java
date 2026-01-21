package sootup.java.core.printer;

import static org.junit.jupiter.api.Assertions.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.graph.MutableControlFlowGraph;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.constant.IntConstant;
import sootup.core.jimple.common.stmt.BranchingStmt;
import sootup.core.jimple.common.stmt.JNopStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.jimple.javabytecode.stmt.JSwitchStmt;
import sootup.core.model.*;
import sootup.core.signatures.MethodSignature;
import sootup.core.util.EscapedWriter;
import sootup.core.util.Utils;
import sootup.core.util.printer.JimplePrinter;
import sootup.core.views.View;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.views.JavaView;

public class LegacyJimplePrinterTest {

  SootClass buildClass(Body.BodyBuilder builder) {

    View view = new JavaView(new EagerInputLocation());

    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("dummyMain", "main", "void", Collections.emptyList());
    Body body =
        builder
            .setMethodSignature(methodSignature)
            .setPosition(NoPositionInformation.getInstance())
            .build();

    ArrayList<String> listOfStrings = Utils.filterJimple(Utils.bodyStmtsAsStrings(body).stream());

    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    return new JavaSootClass(
        new OverridingJavaClassSource(
            Collections.singleton(dummyMainMethod),
            Collections.emptySet(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptySet(),
            null,
            null,
            null,
            null,
            view.getIdentifierFactory().getClassType("dummyMain"),
            new EagerInputLocation()),
        SourceType.Application);
  }

  @Test
  public void printSwitchStmt() {

    StmtPositionInfo noPosInfo = StmtPositionInfo.getNoStmtPositionInfo();
    ArrayList<IntConstant> lookupValues = new ArrayList<>();
    lookupValues.add(IntConstant.getInstance(42));
    lookupValues.add(IntConstant.getInstance(33102));

    final JReturnVoidStmt returnstmt = new JReturnVoidStmt(noPosInfo);
    final JNopStmt jNop = new JNopStmt(noPosInfo);
    final JNopStmt jNop2 = new JNopStmt(noPosInfo);

    BranchingStmt tableSwitch = new JSwitchStmt(IntConstant.getInstance(42), 4, 5, noPosInfo);

    {
      Body.BodyBuilder builder = Body.builder();
      final MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
      controlFlowGraph.setStartingStmt(tableSwitch);

      controlFlowGraph.putEdge(tableSwitch, 0, jNop);
      controlFlowGraph.putEdge(tableSwitch, 1, jNop2);
      controlFlowGraph.putEdge(tableSwitch, 2, returnstmt);

      controlFlowGraph.putEdge(jNop, jNop2);
      controlFlowGraph.putEdge(jNop2, returnstmt);

      SootClass tableClass = buildClass(builder);

      StringWriter sw = new StringWriter();
      new JimplePrinter(JimplePrinter.Option.LegacyMode)
          .printTo(tableClass, new PrintWriter(new EscapedWriter(sw)));

      assertEquals(
          Arrays.asList(
              "public static void main()",
              "tableswitch(42)",
              "case 4: goto label1",
              "case 5: goto label2",
              "default: goto label3",
              "label1:",
              "nop",
              "label2:",
              "nop",
              "label3:",
              "return"),
          Utils.filterJimple(sw.toString()));
    }

    {
      BranchingStmt lookupSwitch =
          new JSwitchStmt(IntConstant.getInstance(123), lookupValues, noPosInfo);

      Body.BodyBuilder builder = Body.builder();
      final MutableControlFlowGraph controlFlowGraph = builder.getControlFlowGraph();
      controlFlowGraph.setStartingStmt(lookupSwitch);

      controlFlowGraph.putEdge(lookupSwitch, 0, jNop);
      controlFlowGraph.putEdge(lookupSwitch, 1, jNop2);
      controlFlowGraph.putEdge(lookupSwitch, 2, returnstmt);

      controlFlowGraph.putEdge(jNop, jNop2);
      controlFlowGraph.putEdge(jNop2, returnstmt);

      SootClass lookupClass = buildClass(builder);

      StringWriter sw2 = new StringWriter();
      new JimplePrinter(JimplePrinter.Option.LegacyMode)
          .printTo(lookupClass, new PrintWriter(new EscapedWriter(sw2)));

      assertEquals(
          Arrays.asList(
              "public static void main()",
              "lookupswitch(123)",
              "case 42: goto label1",
              "case 33102: goto label2",
              "default: goto label3",
              "label1:",
              "nop",
              "label2:",
              "nop",
              "label3:",
              "return"),
          Utils.filterJimple(sw2.toString()));
    }
  }

  @Test
  public void testValidOptions() {
    JimplePrinter p =
        new JimplePrinter(JimplePrinter.Option.UseImports, JimplePrinter.Option.LegacyMode);
    assertThrows(
        RuntimeException.class,
        () -> p.printTo(buildClass(Body.builder()), new PrintWriter(new StringWriter())));
  }
}
