package sootup.java.bytecode.minimaltestsuite.java6.ValidatorTests;

import java.nio.file.Paths;
import java.util.*;

import org.junit.Before;
import org.junit.Test;

import sootup.core.model.Body;

import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.ClassType;
import sootup.core.validation.NewValidator;
import sootup.core.validation.ValidationException;

import sootup.java.bytecode.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.java.core.JavaSootClass;

import sootup.java.core.views.JavaView;
import sootup.jimple.parser.JimpleAnalysisInputLocation;
import sootup.jimple.parser.JimpleProject;
import sootup.jimple.parser.JimpleView;


import static org.junit.Assert.*;

public class NewValidatorTest extends MinimalBytecodeTestSuiteBase {

    NewValidator validator;
    Body body;
    JavaView view;

    @Before
    public void Setup() {

        validator = new NewValidator();

        ClassType classTypeNewValidator =
                new ClassType() {
                    @Override
                    public boolean isBuiltInClass() {
                        return false;
                    }

                    @Override
                    public String getFullyQualifiedName() {
                        return "jimple.NewValidator";
                    }

                    @Override
                    public String getClassName() {
                        return "NewValidator";
                    }

                    @Override
                    public PackageName getPackageName() {
                        return new PackageName("jimple");
                    }
                };

        final ClassType classTypeNewValidator_fail =
                new ClassType() {
                    @Override
                    public boolean isBuiltInClass() {
                        return false;
                    }

                    @Override
                    public String getFullyQualifiedName() {
                        return "jimple.NewValidator_fail";
                    }

                    @Override
                    public String getClassName() {
                        return "NewValidator_fail";
                    }

                    @Override
                    public PackageName getPackageName() {
                        return new PackageName("jimple");
                    }
                };

        final String resourceDir = "src/test/java/resources/";

        // files direct in dir
        final JimpleAnalysisInputLocation inputLocation1 =
                new JimpleAnalysisInputLocation(Paths.get(resourceDir + "/jimple/"));
        JimpleView jv1 = new JimpleProject(inputLocation1).createView();
        final Optional<SootClass<?>> classSource1 = jv1.getClass(classTypeNewValidator);
        assertFalse(classSource1.isPresent());
        final Optional<SootClass<?>> classSource2 = jv1.getClass(classTypeNewValidator_fail);
        assertFalse(classSource2.isPresent());
    }

    @Test
    public void testNewValidator() {
        List<ValidationException> validationExceptions_success = new ArrayList<>();
        List<ValidationException> validationExceptions_fail = new ArrayList<>();

        String classPath = "src/test/java/resources/jimple";
        AnalysisInputLocation<JavaSootClass> jimpleInputLocation =
                new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application

                );
        JimpleView view = new JimpleProject(jimpleInputLocation).createOnDemandView();

        Collection<SootClass<?>> classes = new HashSet<>(); // Set to track the classes to check

        for (SootClass<?> aClass : view.getClasses()) {
            if (!aClass.isLibraryClass()) {
                classes.add(aClass);
            }
        }

        validator.validate(classes.stream().filter(c -> c.getType().getClassName().contains("NewValidator")).findFirst()
                .get().getMethods().stream().filter(m -> m.getName().contains("newValidator"))
                .map(m -> m.getBody()).findFirst().get(),validationExceptions_success);

        assertEquals(0, validationExceptions_success.size());

        validator.validate(classes.stream().filter(c -> c.getType().getClassName().contains("NewValidator_fail")).findFirst()
                .get().getMethods().stream().filter(m -> m.getName().contains("newValidator_fail"))
                .map(m -> m.getBody()).findFirst().get(),validationExceptions_fail);

        assertEquals(1, validationExceptions_fail.size());
    }
}


