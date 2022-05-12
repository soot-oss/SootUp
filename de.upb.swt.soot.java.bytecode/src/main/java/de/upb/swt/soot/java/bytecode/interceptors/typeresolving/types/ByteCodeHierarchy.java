package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;

import java.util.Collection;
import java.util.Collections;

public class ByteCodeHierarchy implements IHierarchy {
    public static boolean isAncestor (Type ancestor, Type child){
        return false;
    }
    public static Collection<Type> getLeastCommonAncestor (Type a, Type b, boolean useWeakObjectType){
        return Collections.emptySet();
    }
}
