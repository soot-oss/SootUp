import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Subject class for {@code TypeUseAnnotationTest}: exercises JSR 308 (TYPE_USE) annotations on
 * method return types and formal parameter types, alongside plain method/parameter declaration
 * annotations, in order to verify the bytecode frontend surfaces type annotations in the model.
 */
// TYPE_USE annotation with CLASS retention -> RuntimeInvisibleTypeAnnotations
@Target(ElementType.TYPE_USE)
@interface TypeUseCls {}

// TYPE_USE annotation with RUNTIME retention -> RuntimeVisibleTypeAnnotations
@Target(ElementType.TYPE_USE)
@Retention(RetentionPolicy.RUNTIME)
@interface TypeUseRt {}

// plain PARAMETER declaration annotation -> RuntimeInvisibleParameterAnnotations
@Target(ElementType.PARAMETER)
@interface ParamDecl {}

// plain METHOD declaration annotation -> RuntimeInvisibleAnnotations
@Target(ElementType.METHOD)
@interface MethodDecl {}

public class TypeUseAnnotation {

  // TYPE_USE annotation on the return type (CLASS retention).
  public @TypeUseCls int returnTypeCls() {
    return 0;
  }

  // TYPE_USE annotation on the return type (RUNTIME retention).
  public @TypeUseRt int returnTypeRt() {
    return 0;
  }

  // Method declaration annotation and return-type annotation on the same method.
  @MethodDecl
  public @TypeUseCls int returnTypeWithMethodDecl() {
    return 0;
  }

  // TYPE_USE annotation on a formal parameter type (CLASS retention).
  public int paramTypeCls(@TypeUseCls int p) {
    return p;
  }

  // TYPE_USE annotation on a formal parameter type (RUNTIME retention).
  public int paramTypeRt(@TypeUseRt int p) {
    return p;
  }

  // Parameter declaration annotation and parameter-type annotation on the same (first) parameter;
  // a second, unannotated parameter guards against off-by-one indexing.
  public int paramDeclAndType(@ParamDecl @TypeUseCls int p, int q) {
    return p + q;
  }
}
