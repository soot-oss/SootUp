package sootup.tests.validator;

import org.junit.Before;
import org.junit.Test;
import sootup.core.model.Body;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.IdentityValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class IdentityValidatorTest {

    IdentityValidator identityValidator;
    JimpleView jimpleView;
    Collection<SootClass> classes;

    @Before
    public void Setup() {

        identityValidator = new IdentityValidator();

        ClassType classTypeFieldRefValidator =
                new ClassType() {
                    @Override
                    public boolean isBuiltInClass() {
                        return false;
                    }

                    @Override
                    public String getFullyQualifiedName() {
                        return "jimple.IdentityValidator";
                    }

                    @Override
                    public String getClassName() {
                        return "IdentityValidator";
                    }

                    @Override
                    public PackageName getPackageName() {
                        return new PackageName("jimple");
                    }
                };

        String classPath = "src/test/resources/validator/jimple";
        JimpleAnalysisInputLocation jimpleInputLocation =
                new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

        jimpleView = new JimpleView(jimpleInputLocation);
        final Optional<SootClass> classSource1 = jimpleView.getClass(classTypeFieldRefValidator);
        assertFalse(classSource1.isPresent());
    }

    @Test
    public void testThisRefSuccess() {
        List<ValidationException> validationExceptions_success = identityValidator.validate(
                getBody("<IdentityValidator: void <init>()>"), jimpleView);

        assertEquals(0, validationExceptions_success.size());
    }

    @Test
    public void testParameterRefSuccess() {
        List<ValidationException> validationExceptions_success = identityValidator.validate(
                getBody("<IdentityValidator: void testParameterRefSuccess(int)>"), jimpleView);

        assertEquals(0, validationExceptions_success.size());
    }

    @Test
    public void testNoThisrRef() {
        List<ValidationException> validationExceptions_success = identityValidator.validate(
                getBody("<IdentityValidator: void testNoThisrRef(int)>"), jimpleView);

        assertEquals(1, validationExceptions_success.size());
    }

    @Test
    public void testParameterRefMultiLocals() {
        List<ValidationException> validationExceptions_success = identityValidator.validate(
                getBody("<IdentityValidator: void testParameterRefMultiLocals(int)>"), jimpleView);

        assertEquals(1, validationExceptions_success.size());
    }

    @Test
    public void testParameterRefNoLocal() {
        List<ValidationException> validationExceptions_success = identityValidator.validate(
                getBody("<IdentityValidator: void testParameterRefNoLocal(int)>"), jimpleView);

        assertEquals(1, validationExceptions_success.size());
    }

    /*
        Unable to test failure cases for out-of-index parameterRef (including the special case where no parameterRef is allowed).
        Attempting to access parameterRef out of bounds may result in Jimple load failure.

        Example:
               public void testNoParameterRefAllowed()
                {
                    LocalsValidator l0;
                    unknown l1;

                    l0 := @this: IdentityValidator;
                    l1 = @parameter0: int;

                    return;
                }
     */

    Body getBody(String methodSignature) {
        return jimpleView.getMethod(jimpleView.getIdentifierFactory()
                .parseMethodSignature(methodSignature) )
                .get().getBody();
    }
}
