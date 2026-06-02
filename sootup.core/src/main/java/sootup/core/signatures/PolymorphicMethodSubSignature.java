package sootup.core.signatures;

import org.jspecify.annotations.NonNull;
import sootup.core.types.Type;

/**
 * A marker subclass for {@code MethodSubSignature}. Used to identify the sub-signature
 * of a polymorphic call site.
 */
public class PolymorphicMethodSubSignature extends MethodSubSignature{
    public PolymorphicMethodSubSignature(@NonNull String name, @NonNull Iterable<? extends Type> parameterTypes, @NonNull Type type) {
        super(name, parameterTypes, type);
    }
}
