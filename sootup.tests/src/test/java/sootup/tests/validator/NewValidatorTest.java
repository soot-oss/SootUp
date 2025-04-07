package sootup.tests.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.core.validation.NewValidator;
import sootup.core.validation.ValidationException;
import sootup.jimple.frontend.JimpleAnalysisInputLocation;
import sootup.jimple.frontend.JimpleView;

public class NewValidatorTest {

  NewValidator validator;
  JimpleView view;
  Collection<SootClass> classes;

  @BeforeEach
  public void Setup() {

    validator = new NewValidator();

    ClassType classTypeNewValidator =
        new ClassType() {

          @Override
          protected boolean isPrimitiveType() {
            return false;
          }

          @Override
          protected boolean isReferenceType() {
            return false;
          }

          @Override
          protected boolean isBottomType() {
            return false;
          }

          @Override
          protected boolean isTopType() {
            return false;
          }

          @Override
          protected boolean isUnknownType() {
            return false;
          }

          @Override
          protected boolean isVoidType() {
            return false;
          }

          @Override
          protected PrimitiveType asPrimitiveType() {
            return null;
          }

          @Override
          protected ReferenceType asReferenceType() {
            return null;
          }

          @Override
          protected Type asBottomType() {
            return null;
          }

          @Override
          protected Type asTopType() {
            return null;
          }

          @Override
          protected UnknownType asUnknownType() {
            return null;
          }

          @Override
          protected VoidType asVoidType() {
            return null;
          }

          @Override
          protected Optional<PrimitiveType> toPrimitiveType() {
            return Optional.empty();
          }

          @Override
          protected Optional<ReferenceType> toReferenceType() {
            return Optional.empty();
          }

          @Override
          protected Optional<Type> toBottomType() {
            return Optional.empty();
          }

          @Override
          protected Optional<Type> toTopType() {
            return Optional.empty();
          }

          @Override
          protected Optional<UnknownType> toUnknownType() {
            return Optional.empty();
          }

          @Override
          protected Optional<VoidType> toVoidType() {
            return Optional.empty();
          }

          @Override
          protected boolean isClassType() {
            return false;
          }

          @Override
          protected boolean isArrayType() {
            return false;
          }

          @Override
          protected boolean isNullType() {
            return false;
          }

          @Override
          protected ClassType asClassType() {
            return null;
          }

          @Override
          protected ArrayType asArrayType() {
            return null;
          }

          @Override
          protected NullType asNullType() {
            return null;
          }

          @Override
          protected Optional<ClassType> toClassType() {
            return Optional.empty();
          }

          @Override
          protected Optional<ArrayType> toArrayType() {
            return Optional.empty();
          }

          @Override
          protected Optional<NullType> toNullType() {
            return Optional.empty();
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

          @Override
          protected boolean isJavaClassType() {
            return false;
          }

          @Override
          protected boolean isModuleJavaClassType() {
            return false;
          }

          @Override
          protected boolean isWeakObjectType() {
            return false;
          }

          @Override
          protected Type asJavaClassType() {
            return null;
          }

          @Override
          protected Type asModuleJavaClassType() {
            return null;
          }

          @Override
          protected Type asWeakObjectType() {
            return null;
          }

          @Override
          protected Optional<Type> toJavaClassType() {
            return Optional.empty();
          }

          @Override
          protected Optional<Type> toModuleJavaClassType() {
            return Optional.empty();
          }

          @Override
          protected Optional<Type> toWeakObjectType() {
            return Optional.empty();
          }
        };

    String classPath = "src/test/resources/validator/jimple";
    JimpleAnalysisInputLocation jimpleInputLocation =
        new JimpleAnalysisInputLocation(Paths.get(classPath), SourceType.Application);

    view = new JimpleView(jimpleInputLocation);
    final Optional<SootClass> classSource1 = view.getClass(classTypeNewValidator);
    assertFalse(classSource1.isPresent());

    classes = new HashSet<>(); // Set to track the classes to check

    view.getClasses().forEach(classes::add);
  }

  @Test
  public void testNewValidatorSuccess() {
    List<ValidationException> validationExceptions_success;

    validationExceptions_success =
        validator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("NewValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("newValidator_pass"))
                .map(m -> m.getBody())
                .findFirst()
                .get(),
            view);

    assertEquals(0, validationExceptions_success.size());
  }

  @Test
  public void testNewValidatorFailure() {
    List<ValidationException> validationExceptions_fail;

    validationExceptions_fail =
        validator.validate(
            classes.stream()
                .filter(c -> c.getType().getClassName().equals("NewValidator"))
                .findFirst()
                .get()
                .getMethods()
                .stream()
                .filter(m -> m.getName().equals("newValidator_fail"))
                .map(m -> m.getBody())
                .findFirst()
                .get(),
            view);

    assertEquals(1, validationExceptions_fail.size());
  }
}
