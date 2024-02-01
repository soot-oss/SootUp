package sootup.tests.validator;

import categories.Java8Test;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import sootup.core.IdentifierFactory;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.Jimple;
import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.jimple.basic.StmtPositionInfo;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JReturnVoidStmt;
import sootup.core.model.*;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.validation.ClassModifiersValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.*;
import sootup.java.core.types.JavaClassType;
import sootup.java.core.views.JavaView;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Category(Java8Test.class)
public class ClassModifiersValidatorTest {
    JavaView view;
    ClassModifiersValidator classModifiersValidator;

    @Before
    public void setUp() {
        view = new JavaView(Collections.singletonList(new EagerInputLocation()));
        classModifiersValidator = new ClassModifiersValidator();
    }

    @Test
    public void testClassModifiersValidator() {
        List<ValidationException> validationExceptions_fail = new ArrayList<>();
        JavaView view = new JavaView(Collections.singletonList(new EagerInputLocation()));
        ClassType type = view.getIdentifierFactory().getClassType("java.lang.String");

        LocalGenerator generator = new LocalGenerator(new HashSet<>());
        MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .getMethodSignature("dummyMain", "main", "void", Collections.emptyList());
        Body.BodyBuilder bodyBuilder = Body.builder();

        final JIdentityStmt firstStmt =
                Jimple.newIdentityStmt(
                        generator.generateLocal(type),
                        Jimple.newParameterRef(type, 0),
                        StmtPositionInfo.getNoStmtPositionInfo());
        final JReturnVoidStmt returnVoidStmt =
                new JReturnVoidStmt(StmtPositionInfo.getNoStmtPositionInfo());

        Body body =
                bodyBuilder
                        .setStartingStmt(firstStmt)
                        .addFlow(firstStmt, returnVoidStmt)
                        .setMethodSignature(methodSignature)
                        .setLocals(generator.getLocals())
                        .build();
        assertEquals(1, body.getLocalCount());

        JavaSootMethod dummyMainMethod =
                new JavaSootMethod(
                        new OverridingBodySource(methodSignature, body),
                        methodSignature,
                        EnumSet.of(MethodModifier.PUBLIC, MethodModifier.STATIC),
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
                                EnumSet.of(ClassModifier.INTERFACE, ClassModifier.ENUM),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()),
                        SourceType.Application);

        assertEquals(mainClass.getMethods().size(), 1);

        classModifiersValidator.validate(mainClass,validationExceptions_fail);

        assertEquals(1, validationExceptions_fail.size());

    }
}
