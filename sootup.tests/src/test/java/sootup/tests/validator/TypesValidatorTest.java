package sootup.tests.validator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.inputlocation.EagerInputLocation;

import sootup.core.jimple.basic.LocalGenerator;
import sootup.core.jimple.basic.NoPositionInformation;

import sootup.core.model.Body;

import sootup.core.model.ClassModifier;
import sootup.core.model.MethodModifier;
import sootup.core.model.SourceType;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.*;
import sootup.core.validation.TypesValidator;
import sootup.core.validation.ValidationException;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.OverridingJavaClassSource;
import sootup.java.core.interceptors.typeresolving.types.AugmentIntegerTypes;
import sootup.java.core.views.JavaView;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TypesValidatorTest {
    static JavaView view;
    static TypesValidator typesValidator;

    @BeforeAll
    public static void setUp() {
        view = new JavaView(Collections.singletonList(new EagerInputLocation()));
        typesValidator = new TypesValidator();
    }

    public JavaSootClass testMethodCreatorWithModifiers(
            EnumSet<MethodModifier> methodModifierEnumSet,
            List<String> methodParameters,
            List<Type> localType){
        JavaView view = new JavaView(Collections.singletonList(new EagerInputLocation()));
        LocalGenerator generator = new LocalGenerator(new HashSet<>());
        for(Type type : localType){
            generator.generateLocal(type);
        }

        MethodSignature methodSignature =
                view.getIdentifierFactory()
                        .getMethodSignature("dummyMain", "main", "void", methodParameters);
        Body.BodyBuilder bodyBuilder = Body.builder();

        Body body =
                bodyBuilder.setMethodSignature(methodSignature).setLocals(generator.getLocals()).build();
        assertEquals(2, body.getLocalCount());

        JavaSootMethod dummyMainMethod =
                new JavaSootMethod(
                        new OverridingBodySource(methodSignature, body),
                        methodSignature,
                        methodModifierEnumSet,
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
                                EnumSet.of(ClassModifier.PUBLIC),
                                Collections.emptyList(),
                                Collections.emptyList(),
                                Collections.emptyList()),
                        SourceType.Application);

        return mainClass;
    }
    @Test
    public void testTypesValidator_success() {
        List<ValidationException> validationExceptions_success = new ArrayList<>();
        List<String> methodParameters = new ArrayList<>();
        methodParameters.add("int a");
        methodParameters.add("string b");

        JavaSootClass javaSootClass =
                testMethodCreatorWithModifiers(
                        EnumSet.of(MethodModifier.PUBLIC),
                        methodParameters,
                        Stream.of(PrimitiveType.getInt(), ArrayType.createArrayType(PrimitiveType.getInt(), 1)).collect(Collectors.toList())
                        );
        Body body = null;
        Optional<JavaSootMethod> opt = javaSootClass.getMethodsByName("main").stream().findFirst();
        if(opt.isPresent()){
            body = opt.get().getBody();
        }

        validationExceptions_success = typesValidator.validate(body, view);

        assertEquals(0, validationExceptions_success.size());
    }

    @Test
    public void testTypesValidator_fail() {
        List<ValidationException> validationExceptions_fail = new ArrayList<>();
        List<String> methodParameters = new ArrayList<>();
        methodParameters.add("void x");
        JavaSootClass javaSootClass =
                testMethodCreatorWithModifiers(
                        EnumSet.of(MethodModifier.PUBLIC),
                        methodParameters,
                        Stream.of(AugmentIntegerTypes.getInteger1(), NullType.getInstance()).collect(Collectors.toList())
                        );
        Body body = null;
        Optional<JavaSootMethod> opt = javaSootClass.getMethodsByName("main").stream().findFirst();
        if(opt.isPresent()){
            body = opt.get().getBody();
        }
        validationExceptions_fail = typesValidator.validate(body, view);

        assertEquals(2, validationExceptions_fail.size());
    }
}
