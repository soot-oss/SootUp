package sootup.jimple.frontend;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.common.*;
import sootup.core.jimple.common.constant.*;
import sootup.core.jimple.common.stmt.*;
import sootup.core.types.ClassType;
import sootup.java.core.views.JavaView;
import sootup.jimple.JimpleParser;

public class JimpleVisitorTest {

  private JimpleBodyConverterState state;
  private ValueVisitor valueVisitor;
  private StmtVisitor stmtVisitor;

  @BeforeEach
  public void setUp() {
    EagerInputLocation loc = new EagerInputLocation();
    JavaView view = new JavaView(loc);
    JimpleConverterUtil util = new JimpleConverterUtil(Paths.get(""));
    ClassType clazz = view.getIdentifierFactory().getClassType("TestClass");

    state = new JimpleBodyConverterState(
        Paths.get(""),
        util,
        clazz,
        view.getIdentifierFactory(),
        new HashMap<>(),
        new HashMap<>()
    );
    valueVisitor = new ValueVisitor(state);
    stmtVisitor = new StmtVisitor(state);
  }

  @Test
  public void testValueVisitorConstant() {
    JimpleParser parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("15"), Paths.get(""));
    Value v = valueVisitor.visitConstant(parser.constant());
      assertInstanceOf(IntConstant.class, v);
    assertEquals(15, ((IntConstant) v).getValue());

    parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("\"test\""), Paths.get(""));
    v = valueVisitor.visitConstant(parser.constant());
      assertInstanceOf(StringConstant.class, v);
    assertEquals("test", ((StringConstant) v).getValue());

    parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("true"), Paths.get(""));
    v = valueVisitor.visitConstant(parser.constant());
      assertInstanceOf(BooleanConstant.class, v);
    assertEquals(BooleanConstant.getInstance(true), v);
  }

  @Test
  public void testValueVisitorImmediate() {
    JimpleParser parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("r0"), Paths.get(""));
    Immediate i = valueVisitor.visitImmediate(parser.immediate());
      assertInstanceOf(Local.class, i);
    assertEquals("r0", ((Local) i).getName());
  }

  @Test
  public void testStmtVisitorAssignment() {
    // We need a full statement for StmtVisitor.visitStatement
    // "r0 = 15;"
    JimpleParser parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("r0 = 15;"), Paths.get(""));
    Stmt s = stmtVisitor.visitStatement(parser.statement());
      assertInstanceOf(JAssignStmt.class, s);
    JAssignStmt as = (JAssignStmt) s;
      assertInstanceOf(Local.class, as.getLeftOp());
    assertEquals("r0", ((Local) as.getLeftOp()).getName());
      assertInstanceOf(IntConstant.class, as.getRightOp());
    assertEquals(15, ((IntConstant) as.getRightOp()).getValue());
  }


  @Test
  public void testStmtVisitorGoto() {
    // "goto L1;"
    JimpleParser parser = JimpleConverterUtil.createJimpleParser(CharStreams.fromString("goto L1;"), Paths.get(""));
    Stmt s = stmtVisitor.visitStatement(parser.statement());
      assertInstanceOf(JGotoStmt.class, s);
  }
}
