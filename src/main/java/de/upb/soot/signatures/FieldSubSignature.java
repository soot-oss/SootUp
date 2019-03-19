package de.upb.soot.signatures;

import javax.annotation.Nonnull;

/**
 * Defines a sub-signature of a field, containing the field name and the type signature.
 *
 * @author Jan Martin Persch
 * @author Jan Martin Persch
 */
public class FieldSubSignature
    extends AbstractClassMemberSubSignature
    implements Comparable<FieldSubSignature>
{
    // region Fields
    
    // endregion /Fields/
    
    // region Constructor
    
    /**
     * Creates a new instance of the {@link FieldSubSignature} class.
     *
     * @param name The method name.
     * @param typeSignature The type signature.
     */
    public FieldSubSignature(@Nonnull String name, @Nonnull TypeSignature typeSignature) {
        super(name, typeSignature);
    }
    
    // endregion /Constructor/
    
    // region Properties
    
    // endregion /Properties/
    
    // region Methods
    
    @Override
    public int compareTo(@Nonnull FieldSubSignature o) {
        return super.compareTo(o);
    }
    
    @Override
    @Nonnull
    public FieldSignature toFullSignature(@Nonnull JavaClassSignature declClassSignature) {
        return new FieldSignature(declClassSignature, this);
    }
    
    // endregion /Methods/
}
