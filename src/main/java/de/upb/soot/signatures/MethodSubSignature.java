package de.upb.soot.signatures;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines a method sub-signature, containing the method name, the parameter type signatures, and the return type signature.
 *
 * @author Jan Martin Persch
 */
public class MethodSubSignature extends AbstractClassMemberSubSignature implements Comparable<MethodSubSignature> {
    // region Fields
    
    // endregion /Fields/
    
    // region Constructor
    
    /**
     * Creates a new instance of the {@link FieldSubSignature} class.
     *
     * @param name The method name.
     * @param parameterSignatures The signatures of the method parameters.
     * @param typeSignature The return type signature.
     */
    public MethodSubSignature(
        @Nonnull String name,
        @Nonnull Iterable<? extends TypeSignature> parameterSignatures,
        @Nonnull TypeSignature typeSignature
    ) {
        super(name, typeSignature);
        
        this._parameterSignatures = ImmutableList.copyOf(parameterSignatures);
    }
    
    // endregion /Constructor/
    
    // region Properties
    
    @Nonnull private final List<TypeSignature> _parameterSignatures;
    
    /**
     * Gets the parameters in an immutable list.
     *
     * @return The value to get.
     */
    @Nonnull
    public List<TypeSignature> getParameterSignatures() {
        return this._parameterSignatures;
    }
    
    // endregion /Properties/
    
    // region Methods
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        if (!super.equals(o)) {
            return false;
        }
        
        MethodSubSignature that = (MethodSubSignature) o;
        
        return Objects.equal(getParameterSignatures(), that.getParameterSignatures());
    }
    
    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getParameterSignatures());
    }
    
    @Override
    public int compareTo(@Nonnull MethodSubSignature o) {
        return super.compareTo(o);
    }
    
    @Override
    @Nonnull
    public MethodSignature toFullSignature(@Nonnull JavaClassSignature declClassSignature) {
        return new MethodSignature(declClassSignature, this);
    }
    
    @Nullable private volatile String _cachedToString;
    
    @Override
    @Nonnull
    public String toString() {
        String cachedToString = this._cachedToString;
        
        if (cachedToString == null) {
            this._cachedToString =
                cachedToString =
                    String.format(
                        "%s %s(%s)",
                        this.getSignature(),
                        this.getName(),
                        this.getParameterSignatures().stream()
                            .map(Object::toString)
                            .collect(Collectors.joining(",")));
        }
        
        return cachedToString;
    }
    
    // endregion /Methods/
}
