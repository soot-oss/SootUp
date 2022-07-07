package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.IntegerType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;


import java.util.Collection;
import java.util.Collections;

public class PrimitiveHierarchy implements IHierarchy {

    /**
     * Calculate the least common ancestor of two types(primitive or BottomType).
     * If there's a = b ==> a is the least common ancestor of a and b;
     * If there's b = a ==> b is the least common ancestor of a and b;
     * If there are c = a and c = b, but there's no b = a or a = b ==> c is the least common ancestor of a and b;
     *
     */
    public static Collection<Type> getLeastCommonAncestor (Type a, Type b){

        if(arePrimitives(a, b)){
            if((a instanceof PrimitiveType.ByteType && b instanceof PrimitiveType.Integer32767Type) || (b instanceof PrimitiveType.ByteType && a instanceof PrimitiveType.Integer32767Type)){
                return Collections.singleton(PrimitiveType.getShort());
            }else if((a instanceof PrimitiveType.CharType && (b instanceof PrimitiveType.ByteType || b instanceof PrimitiveType.ShortType)) ||  (b instanceof PrimitiveType.CharType && (a instanceof PrimitiveType.ByteType || a instanceof PrimitiveType.ShortType))) {
                return Collections.singleton(PrimitiveType.getInt());
            }else if (isAncestor(a, b)){
                return Collections.singleton(a);
            }else if (isAncestor(b, a)){
                return Collections.singleton(b);
            }else{
                return Collections.emptySet();
            }
        }else {
            return Collections.emptySet();
        }
    }

    /**
     * Check the ancestor-relationship between two primitive types <code>ancestor</code> and <code>child</code>, namely,
     * whether child can be assigned to ancestor directly to obtain: ancestor = child
     * Todo[zw]: document of lattice.
     */
    public static boolean isAncestor (Type ancestor, Type child) {

        if (arePrimitives(ancestor, child)) {
            //The following ancestor-relationship checks are based on an type-ancestor-lattice: details in the doc
            if (ancestor.equals(child)) {
                return true;
            } else if (ancestor instanceof PrimitiveType.Integer1Type) {
                return child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.BooleanType || ancestor instanceof PrimitiveType.Integer127Type) {
                return child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.ByteType || ancestor instanceof PrimitiveType.Integer32767Type) {
                return child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.CharType) {
                return child instanceof PrimitiveType.Integer32767Type || child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.ShortType) {
                return child instanceof PrimitiveType.ByteType || child instanceof PrimitiveType.Integer32767Type || child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.IntType) {
                return (!(child instanceof PrimitiveType.BooleanType || child instanceof PrimitiveType.IntType) && (child instanceof IntegerType)) || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.LongType) {
                return (!(child instanceof PrimitiveType.BooleanType) && (child instanceof IntegerType)) || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.FloatType) {
                return (!(child instanceof PrimitiveType.BooleanType) && (child instanceof IntegerType)) || child instanceof PrimitiveType.LongType || child instanceof BottomType;
            } else if (ancestor instanceof PrimitiveType.DoubleType) {
                return (!(child instanceof PrimitiveType.BooleanType) && (child instanceof IntegerType)) || child instanceof PrimitiveType.LongType || child instanceof PrimitiveType.FloatType || child instanceof BottomType;
            } else {
                return false;
            }
        } else if (ancestor instanceof ArrayType && child instanceof ArrayType) {
            //If both types are ArrayType with IntegerType, we may have intermediate type of their bases. Therefore, we may
            //need to check their ancestor-relationship for assign them final type.
            // This ancestor-relationship checks are stricter than below checks, e.g. because a local with type int-array cannot be
            // assigned a local with type short-array
            Type ancestorBase = ((ArrayType) ancestor).getBaseType();
            Type childBase = ((ArrayType) child).getBaseType();
            if (arePrimitives(ancestorBase, childBase)) {
                if (ancestor.equals(child)) {
                    return true;
                } else if (ancestorBase instanceof PrimitiveType.Integer1Type) {
                    return childBase instanceof BottomType;
                } else if (ancestorBase instanceof PrimitiveType.BooleanType || ancestorBase instanceof PrimitiveType.Integer127Type) {
                    return childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
                } else if (ancestorBase instanceof PrimitiveType.ByteType || ancestorBase instanceof PrimitiveType.Integer32767Type) {
                    return childBase instanceof PrimitiveType.Integer127Type || childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
                } else if (ancestorBase instanceof PrimitiveType.CharType || ancestorBase instanceof PrimitiveType.ShortType || ancestorBase instanceof PrimitiveType.IntType) {
                    return childBase instanceof PrimitiveType.Integer32767Type || childBase instanceof PrimitiveType.Integer127Type || childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
                } else {
                    return false;
                }
            }else{
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Check whether the two given types are primitives or BottomType
     */
    private static boolean arePrimitives(Type a, Type b){
        if(a instanceof PrimitiveType || a instanceof BottomType){
            if(b instanceof PrimitiveType || b instanceof BottomType){
                return true;
            }else {
                return false;
            }
        }else {
            return false;
        }
    }
}
