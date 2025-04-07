package sootup.jimple.frontend;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import sootup.core.model.SootClass;
import sootup.core.model.SourceType;
import sootup.core.signatures.PackageName;
import sootup.core.types.*;
import sootup.interceptors.CopyPropagator;

public class JimpleAnalysisInputLocationTest {

  @Test
  public void testClassResolving() {

    ClassType onlyClassNameType =
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
            return "A";
          }

          @Override
          public String getClassName() {
            return "A";
          }

          @Override
          public PackageName getPackageName() {
            return new PackageName("");
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

    final ClassType classType =
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
            return "jimple.A";
          }

          @Override
          public String getClassName() {
            return "A";
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

    final ClassType classTypeFake =
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
            return "jimple.FakeJimple";
          }

          @Override
          public String getClassName() {
            return "FakeJimple";
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

    final String resourceDir = "src/test/java/resources/";

    // files direct in dir
    final JimpleAnalysisInputLocation inputLocation1 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir + "/jimple/"));
    JimpleView jv1 = new JimpleView(inputLocation1);
    final Optional<SootClass> classSource1 = jv1.getClass(onlyClassNameType);
    assertTrue(classSource1.isPresent());
    final Optional<SootClass> classSource2 = jv1.getClass(classType);
    assertFalse(classSource2.isPresent());
    final Optional<SootClass> classSourceNon = jv1.getClass(classTypeFake);
    assertFalse(classSourceNon.isPresent());

    // files in subdir structure
    final JimpleAnalysisInputLocation inputLocation2 =
        new JimpleAnalysisInputLocation(Paths.get(resourceDir));
    JimpleView jv2 = new JimpleView(inputLocation2);
    final Optional<SootClass> classSource3 = jv2.getClass(onlyClassNameType);
    assertFalse(classSource3.isPresent());

    final Optional<SootClass> classSource4 = jv2.getClass(classType);
    assertTrue(classSource4.isPresent());
  }

  @Test
  public void testIfBodyInterceptorsApplied() {
    final String resourceDir = "src/test/java/resources/";
    final JimpleAnalysisInputLocation inputLocation =
        new JimpleAnalysisInputLocation(
            Paths.get(resourceDir + "/jimple/testbodyinterceptorsinjimpleinputlocation"),
            SourceType.Application,
            Arrays.asList(new CopyPropagator()));
    JimpleView jv1 = new JimpleView(inputLocation);
    List<SootClass> applicationClasses = jv1.getClasses().collect(Collectors.toList());
    applicationClasses.forEach(
        cls -> {
          cls.getMethods()
              .forEach(
                  m -> {
                    if (m.getSignature().getName().contains("tc1")) {
                      String s = m.getBody().toString();
                    }
                  });
        });
  }
}
