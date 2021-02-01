package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.model.Field;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.model.SootField;
import de.upb.swt.soot.core.signatures.FieldSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;

import javax.annotation.Nonnull;

public class ArrayElement implements Field {

    @Override
    public FieldSignature getSignature() {
        JavaIdentifierFactory identifierFactory = JavaIdentifierFactory.getInstance();
        ClassType objectType = new JavaClassType("Object", identifierFactory.getPackageName("java.lang"));
        return new FieldSignature(objectType, "element", objectType);
    }
}
