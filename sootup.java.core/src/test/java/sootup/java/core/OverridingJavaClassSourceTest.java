package sootup.java.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import sootup.core.frontend.BodySource;
import sootup.core.inputlocation.EagerInputLocation;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.ClassModifier;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootField;
import sootup.core.model.SootMethod;
import sootup.core.model.SourceType;
import sootup.core.signatures.FieldSignature;
import sootup.core.signatures.FieldSubSignature;
import sootup.core.signatures.MethodSignature;
import sootup.core.signatures.MethodSubSignature;
import sootup.core.types.PrimitiveType;
import sootup.java.core.types.JavaClassType;

class OverridingJavaClassSourceTest {

  private static JavaSootMethod createMethod(JavaClassType classType, String name) {
    MethodSubSignature subSig =
        new MethodSubSignature(name, Collections.emptyList(), PrimitiveType.getInt());
    MethodSignature sig = new MethodSignature(classType, subSig);
    BodySource bodySource =
        new BodySource() {
          @Override
          public Body resolveBody(Iterable<MethodModifier> modifiers) {
            return null;
          }

          @Override
          public Object resolveAnnotationsDefaultValue() {
            return null;
          }

          @Override
          public MethodSignature getSignature() {
            return sig;
          }
        };
    return new JavaSootMethod(
        bodySource,
        sig,
        Collections.emptySet(),
        Collections.emptyList(),
        Collections.emptyList(),
        NoPositionInformation.getInstance());
  }

  private static JavaSootField createField(JavaClassType classType, String name) {
    FieldSubSignature subSig = new FieldSubSignature(name, PrimitiveType.getInt());
    FieldSignature sig = new FieldSignature(classType, subSig);
    return new JavaSootField(sig, Collections.emptySet(), NoPositionInformation.getInstance());
  }

  /**
   * Reproduces class member ordering corruption in OverridingJavaClassSource:
   *
   * <p>Subject pattern: A class with methods [alpha, beta, gamma] or fields [alphaField, betaField,
   * gammaField]. When an interceptor or transformation wraps the class in OverridingJavaClassSource
   * and replaces or updates a member (e.g. withReplacedMethod or withReplacedField), the previous
   * implementation stored members in a java.util.HashSet.
   *
   * <p>Because HashSet does not preserve insertion order, method and field iteration was
   * non-deterministic, leading to shuffled vtable slot assignments, varying field offsets, and
   * non-reproducible binary builds.
   *
   * <p>Using LinkedHashSet guarantees deterministic preservation of member order.
   */
  @Test
  void testPreservesMethodOrderOnReplacement() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaClassType classType = factory.getClassType("com.example.TestClass");
    JavaSootMethod m1 = createMethod(classType, "alpha");
    JavaSootMethod m2 = createMethod(classType, "beta");
    JavaSootMethod m3 = createMethod(classType, "gamma");

    Set<JavaSootMethod> methodSet = new LinkedHashSet<>(Arrays.asList(m1, m2, m3));

    OverridingJavaClassSource initialSource =
        new OverridingJavaClassSource(
            new EagerInputLocation(),
            null,
            classType,
            null,
            Collections.emptySet(),
            null,
            Collections.emptySet(),
            methodSet,
            NoPositionInformation.getInstance(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());

    JavaSootClass sootClass = new JavaSootClass(initialSource, SourceType.Application);
    OverridingJavaClassSource source =
        new OverridingJavaClassSource((JavaSootClassSource) sootClass.getClassSource());

    JavaSootMethod m2Replacement = createMethod(classType, "betaReplacement");
    OverridingJavaClassSource updated = source.withReplacedMethod(m2, m2Replacement);

    List<? extends SootMethod> methods = new ArrayList<>(updated.resolveMethods());
    assertEquals(3, methods.size());
    assertEquals(m1, methods.get(0));
    assertEquals(m3, methods.get(1));
    assertEquals(m2Replacement, methods.get(2));
  }

  @Test
  void testPreservesFieldOrderOnReplacement() {
    JavaIdentifierFactory factory = JavaIdentifierFactory.getInstance();
    JavaClassType classType = factory.getClassType("com.example.TestClass");
    JavaSootField f1 = createField(classType, "alphaField");
    JavaSootField f2 = createField(classType, "betaField");
    JavaSootField f3 = createField(classType, "gammaField");

    Set<JavaSootField> fieldSet = new LinkedHashSet<>(Arrays.asList(f1, f2, f3));

    OverridingJavaClassSource initialSource =
        new OverridingJavaClassSource(
            new EagerInputLocation(),
            null,
            classType,
            null,
            Collections.emptySet(),
            null,
            fieldSet,
            Collections.emptySet(),
            NoPositionInformation.getInstance(),
            EnumSet.of(ClassModifier.PUBLIC),
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList());

    JavaSootClass sootClass = new JavaSootClass(initialSource, SourceType.Application);
    OverridingJavaClassSource source =
        new OverridingJavaClassSource((JavaSootClassSource) sootClass.getClassSource());

    JavaSootField f2Replacement = createField(classType, "betaFieldReplacement");
    OverridingJavaClassSource updated = source.withReplacedField(f2, f2Replacement);

    List<? extends SootField> fields = new ArrayList<>(updated.resolveFields());
    assertEquals(3, fields.size());
    assertEquals(f1, fields.get(0));
    assertEquals(f3, fields.get(1));
    assertEquals(f2Replacement, fields.get(2));
  }
}
