package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.jimple.visitor.TypeVisitor;
import de.upb.swt.soot.core.types.Type;

import javax.annotation.Nonnull;

/**
 * This type is imaginary type, and used for Type Inference
 *
 * @author Zun Wang
 */
public class BottomType extends Type {

    @Nonnull private static final BottomType INSTANCE = new BottomType();

    @Nonnull
    public static BottomType getInstance() {
        return INSTANCE;
    }

    private BottomType() {}

    @Override
    public void accept(@Nonnull TypeVisitor v) {
        //todo: add bottom type case
    }
}
