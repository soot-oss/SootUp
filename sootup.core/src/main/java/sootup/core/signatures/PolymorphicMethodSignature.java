package sootup.core.signatures;

import org.jspecify.annotations.NonNull;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

/**
 * Specialized @code{MethodSignature} class to treat methods with PolymorphicSignature Annotations.
 */
public class PolymorphicMethodSignature extends MethodSignature{

    public static String METHODHANDLE_SIGNATURE = "java.lang.invoke.MethodHandle";
    public static String VARHANDLE_SIGNATURE = "java.lang.invoke.VarHandle";
    public static String POLYMORPHIC_SIGNATURE = "java/lang/invoke/MethodHandle$PolymorphicSignature";

    public PolymorphicMethodSignature(@NonNull ClassType declaringClassSignature, @NonNull String methodName, @NonNull Iterable<Type> parameters, @NonNull Type fqReturnType) {
        super(declaringClassSignature, methodName, parameters, fqReturnType);
    }

    public PolymorphicMethodSignature(@NonNull ClassType declaringClass, @NonNull PolymorphicMethodSubSignature polySubSignature) {
        super(declaringClass, polySubSignature);
    }

    public static @NonNull MethodSignature resolve(@NonNull MethodSignature methodSignature) {
        String methodSigDeclClassType = methodSignature.getDeclClassType().getFullyQualifiedName();
        if (methodSigDeclClassType.equals(METHODHANDLE_SIGNATURE)
                || methodSigDeclClassType.equals(VARHANDLE_SIGNATURE)) {
            // TODO: check if the declaring class type holds a method with the identical name and if this method has the polymorphic annotation
        }
    }
}