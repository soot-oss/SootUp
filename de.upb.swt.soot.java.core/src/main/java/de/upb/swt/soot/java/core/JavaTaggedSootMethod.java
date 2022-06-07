package de.upb.swt.soot.java.core;

import de.upb.swt.soot.core.frontend.BodySource;
import de.upb.swt.soot.core.model.Modifier;
import de.upb.swt.soot.core.model.Position;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.java.core.tag.AbstractHost;

import javax.annotation.Nonnull;

public class JavaTaggedSootMethod extends AbstractHost {

    JavaSootMethod sootMethod;

    public JavaTaggedSootMethod(JavaSootMethod sootMethod){
        this.sootMethod = sootMethod;
    }

    public JavaTaggedSootMethod(@Nonnull BodySource source, @Nonnull MethodSignature methodSignature, @Nonnull Iterable< Modifier > modifiers, @Nonnull Iterable<ClassType> thrownExceptions, @Nonnull Iterable<AnnotationUsage> annotations,  @Nonnull Position position){
          this.sootMethod = new JavaSootMethod(source, methodSignature, modifiers, thrownExceptions, annotations, position);
    }

    public JavaSootMethod getSootMethod() {
        return sootMethod;
    }
}
