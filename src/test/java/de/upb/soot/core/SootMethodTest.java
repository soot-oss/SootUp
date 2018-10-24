package de.upb.soot.core;

import static org.junit.Assert.assertEquals;

import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.util.printer.Printer;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import org.junit.Test;
import org.junit.experimental.categories.Category;

import categories.Java8Test;

@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    IView view = new JavaView(null);
    RefType type = RefType.getInstance("java.lang.String");
    SootClass mainClass = new SootClass(view, null, null, null, null, null, null, null, null);
    SootMethod dummyMainMethod
        = new SootMethod(view, null, null, null, null, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), null);
    // assertEquals("<MainClass: void main(java.lang.String)>", dummyMainMethod.getSignature());
    List<IStmt> stmts=new ArrayList<>();
    LocalGenerator generator = new LocalGenerator();
    stmts.add(Jimple.newIdentityStmt(generator.generateLocal(type), Jimple.newParameterRef(type, 0)));
    stmts.add(Jimple.newAssignStmt(generator.generateLocal(type), Jimple.newNewExpr(type)));
    Body body = new Body(dummyMainMethod, generator.getLocals(), Collections.emptyList(), stmts, null);
    assertEquals(2, body.getLocalCount());
    assertEquals(true, dummyMainMethod.hasActiveBody());
    dummyMainMethod = new SootMethod(dummyMainMethod, body);
    PrintWriter writer=new PrintWriter(System.out);
    Printer printer = new Printer();
    printer.printTo(mainClass, writer);

    // writer.flush();
    // writer.close();
  }
}
