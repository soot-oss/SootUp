package de.upb.swt.soot.java.bytecode.interceptors.typeresolving;

import de.upb.swt.soot.core.types.ArrayType;
import de.upb.swt.soot.core.types.IntegerType;
import de.upb.swt.soot.core.types.PrimitiveType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.java.bytecode.interceptors.BytecodeBodyInterceptors;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.BottomType;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.ByteCodeHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types.WeakObjectType;
import jdk.jfr.Category;
import org.apache.commons.lang3.builder.ToStringExclude;

import java.util.Collection;
import java.util.Collections;
public class AugmentHierarchy implements IHierarchy {

    //todo: check if necessary to add ArrayType case (verified in ByteCodeHierarchy)
    public static Collection<Type> getLeastCommonAncestor (Type a, Type b, boolean useWeakObjectType){
        if(a.equals(b)){
            return Collections.singleton(a);
        } else if (a instanceof BottomType){
            return Collections.singleton(b);
        }else if (b instanceof BottomType){
            return Collections.singleton(a);
        } else if (a instanceof WeakObjectType){
            return Collections.singleton(b);
        } else if (b instanceof WeakObjectType){
            return Collections.singleton(a);
        } else if(a instanceof IntegerType && b instanceof IntegerType) {
            if (a instanceof PrimitiveType.BooleanType || b instanceof PrimitiveType.BooleanType){
                return Collections.emptySet();
            }else if((a instanceof PrimitiveType.ByteType && b instanceof PrimitiveType.Integer32767Type) || (b instanceof PrimitiveType.ByteType || a instanceof PrimitiveType.Integer32767Type)){
                return Collections.singleton(PrimitiveType.getShort());
            } else if ((a instanceof PrimitiveType.CharType && (b instanceof PrimitiveType.ByteType || b instanceof PrimitiveType.ShortType)) ||  (b instanceof PrimitiveType.CharType && (a instanceof PrimitiveType.ByteType || a instanceof PrimitiveType.ShortType))) {
                return Collections.singleton(PrimitiveType.getInt());
            } else if (isAncestor(a, b)){
                return Collections.singleton(a);
            }else {
                return Collections.singleton(b);
            }
        } else if (a instanceof IntegerType || b instanceof IntegerType){
            return Collections.emptySet();
        }else {
            return ByteCodeHierarchy.getLeastCommonAncestor(a, b, useWeakObjectType);
        }
    }

    /**
     * Check the ancestor-relationship between Types <code>ancestor</code> and <code>child</code>
     */
    public static boolean isAncestor (Type ancestor, Type child){
        if(ancestor.equals(child)){
            return true;
        //If both types are ArrayType with IntegerType, we may have intermediate type of their bases. Therefore, we may
        //need to check their ancestor-relationship for assign them final type.
        // This ancestor-relationship checks are stricter than below checks, e.g. because a local with type int-array cannot be
        // assigned a local with type short-array
        }else if (ancestor instanceof ArrayType && child instanceof ArrayType){
            //todo: this logic should be verified in BytecodeHierarchy
            if(((ArrayType) ancestor).getDimension() != ((ArrayType) child).getDimension()){
                return false;
            }
            Type ancestorBase = ((ArrayType) ancestor).getBaseType();
            Type childBase = ((ArrayType) child).getBaseType();
            if(ancestorBase instanceof PrimitiveType.Integer1Type){
                return childBase instanceof BottomType;
            }else if (ancestorBase instanceof PrimitiveType.BooleanType || ancestorBase instanceof PrimitiveType.Integer127Type){
                return childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
            }else if (ancestorBase instanceof PrimitiveType.ByteType || ancestorBase instanceof PrimitiveType.Integer32767Type){
                return childBase instanceof PrimitiveType.Integer127Type || childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
            } else if (ancestorBase instanceof PrimitiveType.CharType || ancestorBase instanceof PrimitiveType.ShortType || ancestorBase instanceof PrimitiveType.IntType){
                return childBase instanceof PrimitiveType.Integer32767Type || childBase instanceof PrimitiveType.Integer127Type || childBase instanceof PrimitiveType.Integer1Type || childBase instanceof BottomType;
            }else if(childBase instanceof IntegerType){
                return false;
            }else{
                return ByteCodeHierarchy.isAncestor(ancestor, child);
            }
        //The following ancestor-relationship checks are based on an type-ancestor-lattice: details in the doc
        //Todo: document of lattice.
        }else if(ancestor instanceof PrimitiveType.Integer1Type){
                return child instanceof BottomType;
        }else if(ancestor instanceof PrimitiveType.BooleanType || ancestor instanceof PrimitiveType.Integer127Type){
                return child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
        }else if (ancestor instanceof PrimitiveType.ByteType || ancestor instanceof PrimitiveType.Integer32767Type){
                return child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
        }else if (ancestor instanceof PrimitiveType.CharType){
                return child instanceof PrimitiveType.Integer32767Type || child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
        }else if (ancestor instanceof PrimitiveType.ShortType){
                return child instanceof PrimitiveType.ByteType || child instanceof PrimitiveType.Integer32767Type || child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
        }else if (ancestor instanceof PrimitiveType.IntType){
                return child instanceof PrimitiveType.CharType || child instanceof PrimitiveType.ShortType ||child instanceof PrimitiveType.ByteType || child instanceof PrimitiveType.Integer32767Type || child instanceof PrimitiveType.Integer127Type || child instanceof PrimitiveType.Integer1Type || child instanceof BottomType;
        }else if(child instanceof IntegerType){
                return false;
        }else{
                return ByteCodeHierarchy.isAncestor(ancestor, child);
        }
    }
}
