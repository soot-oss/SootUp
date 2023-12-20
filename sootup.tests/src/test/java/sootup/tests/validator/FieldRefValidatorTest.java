package sootup.tests.validator;

import org.junit.Before;
import org.junit.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.FieldRefValidator;
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

public class FieldRefValidatorTest {

    FieldRefValidator fieldRefValidator;
    JimpleView jimpleView;
    Collection<SootClass<?>> classes;

    @Before
    public void Setup() {

        fieldRefValidator = new FieldRefValidator();

        ClassType classTypeNewValidator =
                new ClassType() {
                    @Override
                    public boolean isBuiltInClass() {
                        return false;
                    }

                    @Override
                    public String getFullyQualifiedName() {
                        return "jimple.FieldRefValidator";
                    }

                    @Override
                    public String getClassName() {
                        return "FieldRefValidator";
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
        final Optional<SootClass<?>> classSource1 = jimpleView.getClass(classTypeNewValidator);
        assertFalse(classSource1.isPresent());

        classes = new HashSet<>(); // Set to track the classes to check

        for (SootClass<?> aClass : jimpleView.getClasses()) {
            if (!aClass.isLibraryClass()) {
                classes.add(aClass);
            }
        }
    }

    @Test
    public void testFieldRefValidatorSuccess() {
        List<ValidationException> validationExceptions_success;

        validationExceptions_success =
                fieldRefValidator.validate(
                        classes.stream()
                                .filter(c -> c.getType().getClassName().equals("FieldRefValidator"))
                                .findFirst()
                                .get()
                                .getMethods()
                                .stream()
                                .filter(m -> m.getName().equals("fieldRefVariable"))
                                .map(m -> m.getBody())
                                .findFirst()
                                .get(),
                        jimpleView);

        assertEquals(1, validationExceptions_success.size());
//        System.out.println(validationExceptions_success.get(0).getMessage());
    }
}
