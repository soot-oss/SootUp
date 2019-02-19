package de.upb.soot.core;

import categories.Java8Test;
import de.upb.soot.frontends.ClassSource;
import de.upb.soot.frontends.java.WalaIRMethodSourceContent;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.jimple.common.type.RefType;
import de.upb.soot.jimple.common.type.Type;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * 
 * @author Linghui Luo
 *
 */
@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    IView view = new JavaView(null);
    Type type = view.getType(view.getSignatureFactory().getTypeSignature("java.lang.String"));

    PositionInfo nop = PositionInfo.createNoPositionInfo();
    List<IStmt> stmts = new ArrayList<>();
    LocalGenerator generator = new LocalGenerator();
    stmts.add(Jimple.newIdentityStmt(generator.generateLocal(type), Jimple.newParameterRef(type, 0), nop));
    stmts.add(Jimple.newAssignStmt(generator.generateLocal(type), Jimple.newNewExpr((RefType) type), nop));

    Body body = new Body(generator.getLocals(), Collections.emptyList(), stmts, null);

    assertEquals(2, body.getLocalCount());

    SootMethod dummyMainMethod;
    dummyMainMethod = new SootMethod(view, null,
        new WalaIRMethodSourceContent(
            view.getSignatureFactory().getMethodSignature("main", "dummyMain", "void", Collections.emptyList())),
        Collections.emptyList(), view.getSignatureFactory().getTypeSignature("void"),
        EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), null);
    dummyMainMethod = new SootMethod(dummyMainMethod, body);
    assertTrue(dummyMainMethod.hasActiveBody());
    SootClass mainClass = new SootClass(view, ResolvingLevel.BODIES,
        new ClassSource(new JavaSourcePathNamespace(""), null, view.getSignatureFactory().getClassSignature("dummyMain")),
        ClassType.Application, null, Collections.emptySet(), null, Collections.emptySet(),
        Collections.singleton(dummyMainMethod), null, EnumSet.of(Modifier.PUBLIC));

    assertEquals(mainClass.getMethods().size(), 1);
  }
}
