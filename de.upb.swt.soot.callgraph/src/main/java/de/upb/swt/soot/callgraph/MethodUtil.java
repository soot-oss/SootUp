package de.upb.swt.soot.callgraph;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.model.SootMethod;
import de.upb.swt.soot.core.signatures.MethodSignature;
import de.upb.swt.soot.core.views.View;

import javax.annotation.Nullable;

public class MethodUtil {

    @Nullable
    public static SootMethod methodSignatureToMethod(View<? extends SootClass> view, MethodSignature methodSignature) {
        SootMethod currentMethodCandidate =
                view.getClass(methodSignature.getDeclClassType())
                        .flatMap(c -> c.getMethod(methodSignature))
                        .orElse(null);
        return currentMethodCandidate;
    }
}
