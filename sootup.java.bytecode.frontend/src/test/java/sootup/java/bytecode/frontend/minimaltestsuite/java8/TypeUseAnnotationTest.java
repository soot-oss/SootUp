package sootup.java.bytecode.frontend.minimaltestsuite.java8;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import sootup.core.jimple.common.Local;
import sootup.core.model.Body;
import sootup.java.bytecode.frontend.minimaltestsuite.MinimalBytecodeTestSuiteBase;
import sootup.java.core.AnnotationUsage;
import sootup.java.core.JavaSootClass;
import sootup.java.core.JavaSootMethod;
import sootup.java.core.jimple.basic.JavaLocal;

/**
 * Verifies that JSR 308 (TYPE_USE) annotations declared on method return types and formal parameter
 * types survive into the SootUp model when read from bytecode. Return-type annotations are expected
 * on the method; formal-parameter-type annotations are expected on the corresponding parameter
 * {@link JavaLocal} (mirroring where plain parameter declaration annotations land). Both CLASS
 * (RuntimeInvisibleTypeAnnotations) and RUNTIME (RuntimeVisibleTypeAnnotations) retention are
 * covered.
 *
 * @see sootup.java.bytecode.frontend.conversion.AsmMethodSource#resolveReturnTypeAnnotations()
 */
public class TypeUseAnnotationTest extends MinimalBytecodeTestSuiteBase {

  private AnnotationUsage annotation(String name) {
    return new AnnotationUsage(identifierFactory.getClassType(name), Collections.emptyMap());
  }

  private JavaSootMethod method(JavaSootClass sootClass, String name) {
    Optional<JavaSootMethod> method = sootClass.getMethod(name, Collections.emptyList());
    assertTrue(method.isPresent(), "expected method " + name + " to be present");
    return method.get();
  }

  private JavaSootMethod methodWithIntParams(
      JavaSootClass sootClass, String name, int parameterCount) {
    List<String> parameterTypes = Collections.nCopies(parameterCount, "int");
    Optional<JavaSootMethod> method =
        sootClass.getMethod(
            identifierFactory
                .getMethodSignature(sootClass.getType(), name, "int", parameterTypes)
                .getSubSignature());
    assertTrue(method.isPresent(), "expected method " + name + " to be present");
    return method.get();
  }

  /** TYPE_USE annotation on a return type with CLASS retention. */
  @Test
  public void testReturnTypeAnnotationClassRetention() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Collections.singletonList(annotation("TypeUseCls")),
        method(sootClass, "returnTypeCls").getAnnotations());
  }

  /** TYPE_USE annotation on a return type with RUNTIME retention. */
  @Test
  public void testReturnTypeAnnotationRuntimeRetention() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Collections.singletonList(annotation("TypeUseRt")),
        method(sootClass, "returnTypeRt").getAnnotations());
  }

  /**
   * A method carrying both a declaration annotation (@MethodDecl) and a return-type annotation
   * (@TypeUseCls): both must be present, with the declaration annotation first.
   */
  @Test
  public void testMethodDeclarationAndReturnTypeAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    assertEquals(
        Arrays.asList(annotation("MethodDecl"), annotation("TypeUseCls")),
        method(sootClass, "returnTypeWithMethodDecl").getAnnotations());
  }

  /** TYPE_USE annotation on a formal parameter type with CLASS retention. */
  @Test
  public void testParameterTypeAnnotationClassRetention() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Body body = methodWithIntParams(sootClass, "paramTypeCls", 1).getBody();
    JavaLocal parameterLocal = (JavaLocal) body.getParameterLocal(0);
    assertEquals(
        Collections.singletonList(annotation("TypeUseCls")), parameterLocal.getAnnotations());
  }

  /** TYPE_USE annotation on a formal parameter type with RUNTIME retention. */
  @Test
  public void testParameterTypeAnnotationRuntimeRetention() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Body body = methodWithIntParams(sootClass, "paramTypeRt", 1).getBody();
    JavaLocal parameterLocal = (JavaLocal) body.getParameterLocal(0);
    assertEquals(
        Collections.singletonList(annotation("TypeUseRt")), parameterLocal.getAnnotations());
  }

  /**
   * A parameter carrying both a declaration annotation (@ParamDecl) and a type annotation
   * (@TypeUseCls): both must be attached to the parameter local, with the declaration annotation
   * first. A second, unannotated parameter must have no annotations.
   */
  @Test
  public void testParameterDeclarationAndTypeAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Body body = methodWithIntParams(sootClass, "paramDeclAndType", 2).getBody();

    JavaLocal annotatedParam = (JavaLocal) body.getParameterLocal(0);
    assertEquals(
        Arrays.asList(annotation("ParamDecl"), annotation("TypeUseCls")),
        annotatedParam.getAnnotations());

    JavaLocal plainParam = (JavaLocal) body.getParameterLocal(1);
    assertEquals(Collections.emptyList(), plainParam.getAnnotations());
  }

  /**
   * TYPE_USE annotation on a local variable type (JSR 308 LOCAL_VARIABLE target): the annotation
   * must be attached to the corresponding Jimple local. Exactly one local carries it.
   */
  @Test
  public void testLocalVariableTypeAnnotation() {
    JavaSootClass sootClass = loadClass(getDeclaredClassSignature());
    Body body = method(sootClass, "localTypeCls").getBody();
    List<AnnotationUsage> annotationsOnLocals = new ArrayList<>();
    for (Local l : body.getLocals()) {
      if (l instanceof JavaLocal jl) {
        for (AnnotationUsage a : jl.getAnnotations()) {
          annotationsOnLocals.add(a);
        }
      }
    }
    assertEquals(Collections.singletonList(annotation("TypeUseCls")), annotationsOnLocals);
  }
}
