package de.upb.soot.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.Project;
import de.upb.soot.frontends.JavaClassSource;
import de.upb.soot.frontends.java.WalaIRMethodSourceContent;
import de.upb.soot.jimple.Jimple;
import de.upb.soot.jimple.basic.LocalGenerator;
import de.upb.soot.jimple.basic.PositionInfo;
import de.upb.soot.jimple.common.stmt.IStmt;
import de.upb.soot.namespaces.JavaSourcePathNamespace;
import de.upb.soot.signatures.DefaultSignatureFactory;
import de.upb.soot.signatures.JavaClassSignature;
import de.upb.soot.signatures.MethodSignature;
import de.upb.soot.views.IView;
import de.upb.soot.views.JavaView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    IView view = new JavaView(new Project(null, new DefaultSignatureFactory()));
    JavaClassSignature type = view.getSignatureFactory().getClassSignature("java.lang.String");

    List<IStmt> stmts = new ArrayList<>();
    LocalGenerator generator = new LocalGenerator();
    stmts.add(
        Jimple.newIdentityStmt(
            generator.generateLocal(type),
            Jimple.newParameterRef(type, 0),
            PositionInfo.createNoPositionInfo()));
    stmts.add(
        Jimple.newAssignStmt(
            generator.generateLocal(type),
            Jimple.newNewExpr(type),
            PositionInfo.createNoPositionInfo()));

    Body body = new Body(generator.getLocals(), Collections.emptyList(), stmts, null);

    assertEquals(2, body.getLocalCount());

    MethodSignature methodSignature =
        view.getSignatureFactory()
            .getMethodSignature("main", "dummyMain", "void", Collections.emptyList());
    SootMethod dummyMainMethod =
        new SootMethod(
            new WalaIRMethodSourceContent(methodSignature),
            methodSignature,
            EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
            Collections.emptyList(),
            body,
            null);

    SootClass mainClass =
        new SootClass(
            ResolvingLevel.BODIES,
            new JavaClassSource(
                new JavaSourcePathNamespace(Collections.emptySet()),
                null,
                view.getSignatureFactory().getClassSignature("dummyMain")),
            ClassType.Application,
            null,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            Collections.singleton(dummyMainMethod),
            null,
            EnumSet.of(Modifier.PUBLIC));

    assertEquals(mainClass.getMethods().size(), 1);
    assertTrue(
        mainClass
            .getMethod(methodSignature)
            .orElseThrow(() -> new RuntimeException("Failed getting method " + methodSignature))
            .hasActiveBody());
  }
}
