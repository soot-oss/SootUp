package dexpler;

import Util.DexUtil;
import Util.Util;
import org.jf.dexlib2.iface.Method;
import sootup.core.frontend.BodySource;
import sootup.core.frontend.OverridingBodySource;
import sootup.core.frontend.ResolveException;
import sootup.core.graph.MutableStmtGraph;
import sootup.core.jimple.basic.Local;
import sootup.core.jimple.basic.NoPositionInformation;
import sootup.core.model.Body;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootMethod;
import sootup.core.signatures.MethodSignature;
import sootup.core.types.ClassType;
import sootup.core.types.Type;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class DexMethodSource implements BodySource {

    private final ClassType classType;
    private final Set<Local> locals;
    private final MutableStmtGraph mutableStmtGraph;
    private final Method method;
    private final List<Type> parameterTypes;

    public DexMethodSource(ClassType classType, Set<Local> locals, MutableStmtGraph mutableStmtGraph, Method method, List<Type> parameterTypes){
        this.classType = classType;
        this.locals = locals;
        this.mutableStmtGraph = mutableStmtGraph;
        this.method = method;
        this.parameterTypes = parameterTypes;
    }
    @Nonnull
    @Override
    public Body resolveBody(@Nonnull Iterable<MethodModifier> modifiers) throws ResolveException, IOException {
        Body.BodyBuilder bodyBuilder = Body.builder(mutableStmtGraph).setMethodSignature(getSignature()).setPosition(NoPositionInformation.getInstance()).setLocals(locals);
        return bodyBuilder.build();
    }

    public SootMethod makeSootMethod(){
        SootMethod sootMethod;
        EnumSet<MethodModifier> methodModifiers = getMethodModifiers(method.getAccessFlags());
        try {
            sootMethod = new SootMethod(
                    new OverridingBodySource(getSignature(), resolveBody(methodModifiers)),
                    getSignature(),
                    methodModifiers,
                    Collections.emptyList(),
                    NoPositionInformation.getInstance()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return sootMethod;
    }

    public static EnumSet<MethodModifier> getMethodModifiers(int access) {
        EnumSet<MethodModifier> modifierEnumSet = EnumSet.noneOf(MethodModifier.class);

        // add all modifiers for which (access & ABSTRACT) =! 0
        for (MethodModifier modifier : MethodModifier.values()) {
            if ((access & modifier.getBytecode()) != 0) {
                modifierEnumSet.add(modifier);
            }
        }
        return modifierEnumSet;
    }

    @Override
    public Object resolveAnnotationsDefaultValue() {
        return null;
    }

    @Nonnull
    @Override
    public MethodSignature getSignature() {
        String className = classType.getClassName();
        if(Util.isByteCodeClassName(className)){
            className = Util.dottedClassName(className);
        }
        return new MethodSignature(classType, className, parameterTypes, DexUtil.toSootType(method.getReturnType(), 0));
    }
}
