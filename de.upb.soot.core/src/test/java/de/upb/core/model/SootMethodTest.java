package de.upb.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.soot.core.DefaultIdentifierFactory;
import de.upb.soot.core.Project;
import de.upb.soot.core.frontend.EagerJavaClassSource;
import de.upb.soot.core.frontend.EagerMethodSource;
import de.upb.soot.core.inputlocation.EagerInputLocation;
import de.upb.soot.core.jimple.Jimple;
import de.upb.soot.core.jimple.basic.LocalGenerator;
import de.upb.soot.core.jimple.basic.PositionInfo;
import de.upb.soot.core.jimple.common.stmt.Stmt;
import de.upb.soot.core.model.*;
import de.upb.soot.core.signatures.MethodSignature;
import de.upb.soot.core.types.JavaClassType;
import de.upb.soot.core.views.JavaView;
import de.upb.soot.core.views.View;
import java.util.*;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/** @author Linghui Luo */
@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    DefaultIdentifierFactory factories = DefaultIdentifierFactory.getInstance();
    View view = new JavaView<>(new Project<>(null, factories));
    JavaClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

    List<Stmt> stmts = new ArrayList<>();
    LocalGenerator generator = new LocalGenerator(new HashSet<>());
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
        view.getIdentifierFactory()
            .getMethodSignature("main", "dummyMain", "void", Collections.emptyList());
    SootMethod dummyMainMethod =
        new SootMethod(
            new EagerMethodSource(methodSignature, body),
            methodSignature,
            EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
            Collections.emptyList());

    SootClass mainClass =
        new SootClass(
            new EagerJavaClassSource(
                new EagerInputLocation("irrelevant-test-path/"),
                null,
                view.getIdentifierFactory().getClassType("dummyMain"),
                null,
                Collections.emptySet(),
                null,
                Collections.emptySet(),
                Collections.singleton(dummyMainMethod),
                null,
                EnumSet.of(Modifier.PUBLIC)),
            SourceType.Application);

    assertEquals(mainClass.getMethods().size(), 1);
    assertTrue(
        mainClass
            .getMethod(methodSignature)
            .orElseThrow(() -> new RuntimeException("Failed getting method " + methodSignature))
            .hasBody());
  }
}
