package sootup.core.signatures;

import org.jspecify.annotations.NonNull;
import sootup.core.types.Type;

/**
 * Specialized @code{MethodSubSignature} class to treat methods with PolymorphicSignature Annotations.
 */
public class PolymorphicMethodSubSignature extends MethodSubSignature{

    public PolymorphicMethodSubSignature(@NonNull String name, @NonNull Iterable<? extends Type> parameterTypes, @NonNull Type type) {
        super(name, parameterTypes, type);
    }
}
