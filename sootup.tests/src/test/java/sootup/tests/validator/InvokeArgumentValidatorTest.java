package sootup.tests.validator;

import org.junit.Before;
import org.junit.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.InvokeArgumentValidator;
import sootup.core.validation.ValidationException;
import sootup.java.bytecode.inputlocation.DefaultRTJarAnalysisInputLocation;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleView;

import java.nio.file.Paths;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class InvokeArgumentValidatorTest {
    InvokeArgumentValidator invokeArgumentValidator;
    JimpleView jimpleView;
    Collection<SootClass> classes;

    @Before
    public void Setup() {

        invokeArgumentValidator = new InvokeArgumentValidator();

        ClassType classTypeFieldRefValidator =
                new ClassType() {
                    @Override
                    public boolean isBuiltInClass() {
                        return false;
                    }

                    @Override
                    public String getFullyQualifiedName() {
                        return "jimple.InvokeArgumentValidator";
                    }

                    @Override
                    public String getClassName() {
                        return "InvokeArgumentValidator";
                    }

                    @Override
                    public PackageName getPackageName() {
                        return new PackageName("jimple");
                    }
                };

        String classPath = "src/test/resources/validator/jimple";
        JimpleAnalysisInputLocation jimpleInputLocation =
                new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

        // rt.jar is required since the validator uses typeHierarchy
        DefaultRTJarAnalysisInputLocation defaultRTJarAnalysisInputLocation = new DefaultRTJarAnalysisInputLocation();
        jimpleView = new JimpleView(Arrays.asList(jimpleInputLocation, defaultRTJarAnalysisInputLocation));

        final Optional<SootClass> classSource1 = jimpleView.getClass(classTypeFieldRefValidator);
        assertFalse(classSource1.isPresent());

        classes = new HashSet<>(); // Set to track the classes to check

        for (SootClass aClass : jimpleView.getClasses()) {
            if (!aClass.isLibraryClass()) {
                classes.add(aClass);
            }
        }
    }

    @Test
    public void invokeArgumentValidator_success() {
        List<ValidationException> validationExceptions_success;

        validationExceptions_success = invokeArgumentValidator.validate( jimpleView.getMethod(
                jimpleView.getIdentifierFactory()
                        .parseMethodSignature("<InvokeArgumentValidator: void invokeArgumentValidator_success()>") )
                .get().getBody(), jimpleView);

        assertEquals(0, validationExceptions_success.size());
    }

    @Test
    public void testArgumentNumber_fail() {
        List<ValidationException> validationExceptions_success;

        validationExceptions_success = invokeArgumentValidator.validate( jimpleView.getMethod(
                        jimpleView.getIdentifierFactory()
                                .parseMethodSignature("<InvokeArgumentValidator: void testArgumentNumber_fail()>") )
                .get().getBody(), jimpleView);
        assertEquals(1, validationExceptions_success.size());
    }

    @Test
    public void testArgumentType_fail() {
        List<ValidationException> validationExceptions_success;

        validationExceptions_success = invokeArgumentValidator.validate( jimpleView.getMethod(
                        jimpleView.getIdentifierFactory()
                                .parseMethodSignature("<InvokeArgumentValidator: void testArgumentType_fail()>") )
                .get().getBody(), jimpleView);
        assertEquals(2, validationExceptions_success.size());
    }

}
