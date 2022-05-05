package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.WeakObjectType;

import java.util.Collection;
import java.util.Collections;

public class AugmentHierarchy implements IHierarchy {
    public static Collection<Type> getLeastCommonAncestor (Type a, Type b, boolean useWeakObjectType){
        if(a.equals(b)){
            return Collections.singleton(a);
        }else if (a instanceof BottomType){
            return Collections.singleton(b);
        }else if (b instanceof BottomType){
            return Collections.singleton(a);
        }else if (a instanceof WeakObjectType){
            return Collections.singleton(b);
        }else if (b instanceof WeakObjectType){
            return Collections.singleton(a);
        }
    }

    public static boolean isAncestor (Type ancestor, Type child){
        if(ancestor.equals(child)){
            return true;
        }else if (ancestor instanceof ArrayType && child instanceof ArrayType){
            Type ancestorBase = ((ArrayType) ancestor).getBaseType();
            Type childBase = ((ArrayType) child).getBaseType();

        }
    }
}
