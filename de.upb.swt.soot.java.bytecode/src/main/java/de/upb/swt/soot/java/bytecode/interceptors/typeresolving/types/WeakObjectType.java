package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.jimple.visitor.TypeVisitor;
import de.upb.swt.soot.core.types.ReferenceType;

import javax.annotation.Nonnull;

/**
 * This type is used for Type Inference.
 * Object, Serializable, Cloneable are weak object types.
 *
 * @author Zun Wang
 */
public class WeakObjectType extends ReferenceType {

    @Nonnull private static final WeakObjectType INSTANCE = new WeakObjectType();

    @Nonnull public static WeakObjectType getInstance(){
        return INSTANCE;
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
        //todo: weak objects type case
    }

}
