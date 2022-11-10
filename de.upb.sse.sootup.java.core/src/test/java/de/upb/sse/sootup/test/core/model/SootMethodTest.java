package de.upb.sse.sootup.test.core.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import categories.Java8Test;
import de.upb.sse.sootup.core.Project;
import de.upb.sse.sootup.core.frontend.OverridingBodySource;
import de.upb.sse.sootup.core.inputlocation.EagerInputLocation;
import de.upb.sse.sootup.core.jimple.Jimple;
import de.upb.sse.sootup.core.jimple.basic.LocalGenerator;
import de.upb.sse.sootup.core.jimple.basic.NoPositionInformation;
import de.upb.sse.sootup.core.jimple.basic.StmtPositionInfo;
import de.upb.sse.sootup.core.jimple.common.stmt.JIdentityStmt;
import de.upb.sse.sootup.core.jimple.common.stmt.JReturnVoidStmt;
import de.upb.sse.sootup.core.model.Body;
import de.upb.sse.sootup.core.model.Modifier;
import de.upb.sse.sootup.core.model.SourceType;
import de.upb.sse.sootup.core.signatures.MethodSignature;
import de.upb.sse.sootup.core.types.ClassType;
import de.upb.sse.sootup.core.views.View;
import de.upb.sse.sootup.java.core.JavaProject;
import de.upb.sse.sootup.java.core.JavaSootClass;
import de.upb.sse.sootup.java.core.JavaSootMethod;
import de.upb.sse.sootup.java.core.OverridingJavaClassSource;
import de.upb.sse.sootup.java.core.language.JavaLanguage;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import org.junit.Test;
import org.junit.experimental.categories.Category;

/**
 * @author Linghui Luo
 * @author Kaustubh Kelkar updated on 02.07.2020
 */
@Category(Java8Test.class)
public class SootMethodTest {

  @Test
  public void testCreateMethod() {
    Project project =
        JavaProject.builder(new JavaLanguage(8)).addInputLocation(new EagerInputLocation()).build();
    View view = project.createOnDemandView();
    ClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

    LocalGenerator generator = new LocalGenerator(new LinkedHashSet<>());
    MethodSignature methodSignature =
        view.getIdentifierFactory()
            .getMethodSignature("main", "dummyMain", "void", Collections.emptyList());
    Body.BodyBuilder bodyBuilder = Body.builder();

    final JIdentityStmt firstStmt =
        Jimple.newIdentityStmt(
            generator.generateLocal(type),
            Jimple.newParameterRef(type, 0),
            StmtPositionInfo.createNoStmtPositionInfo());
    final JReturnVoidStmt returnVoidStmt =
        new JReturnVoidStmt(StmtPositionInfo.createNoStmtPositionInfo());

    Body body =
        bodyBuilder
            .setStartingStmt(firstStmt)
            .addFlow(firstStmt, returnVoidStmt)
            .setMethodSignature(methodSignature)
            .setLocals(generator.getLocals())
            .setTraps(Collections.emptyList())
            .build();
    assertEquals(1, body.getLocalCount());

    JavaSootMethod dummyMainMethod =
        new JavaSootMethod(
            new OverridingBodySource(methodSignature, body),
            methodSignature,
            EnumSet.of(Modifier.PUBLIC, Modifier.STATIC),
            Collections.emptyList(),
            Collections.emptyList(),
            NoPositionInformation.getInstance());

    JavaSootClass mainClass =
        new JavaSootClass(
            new OverridingJavaClassSource(
                new EagerInputLocation(),
                null,
                view.getIdentifierFactory().getClassType("dummyMain"),
                null,
                Collections.emptySet(),
                null,
                Collections.emptySet(),
                Collections.singleton(dummyMainMethod),
                NoPositionInformation.getInstance(),
                EnumSet.of(Modifier.PUBLIC),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()),
            SourceType.Application);

    assertEquals(mainClass.getMethods().size(), 1);

    assertTrue(
        mainClass
            .getMethod(methodSignature.getSubSignature())
            .orElseThrow(() -> new RuntimeException("Failed getting method " + methodSignature))
            .hasBody());
  }
}
