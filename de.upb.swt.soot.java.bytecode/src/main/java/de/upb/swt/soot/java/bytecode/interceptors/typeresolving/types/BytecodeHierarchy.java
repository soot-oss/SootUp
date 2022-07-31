package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.PrimitiveHierarchy;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class BytecodeHierarchy implements IHierarchy {

    private ViewTypeHierarchy typeHierarchy;

    public BytecodeHierarchy(View<? extends SootClass> view){
        this.typeHierarchy = new ViewTypeHierarchy(view);
    }

    public boolean isAncestor (Type ancestor, Type child){

        boolean isAncestor = PrimitiveHierarchy.isAncestor(ancestor, child);
        if(!isAncestor && !(ancestor instanceof PrimitiveType && child instanceof PrimitiveType)){
            if(ancestor.equals(child)){
                isAncestor = true;
            //ancestor is reference, child is primitive
            } else if(ancestor instanceof ReferenceType && child instanceof PrimitiveType){
                if(ancestor instanceof ClassType){
                    ClassType anc_ct = (ClassType) ancestor;
                    PrimitiveType chi_pt = (PrimitiveType) child;
                    if(isBoxOrUnbox(anc_ct, chi_pt)){
                        isAncestor = true;
                    }
                }
            //ancestor is primitive, child is reference
            }else if(ancestor instanceof PrimitiveType && child instanceof ReferenceType){
                if(child instanceof ClassType){
                    PrimitiveType anc_ct = (PrimitiveType) ancestor;
                    ClassType chi_pt = (ClassType) child;
                    if(isBoxOrUnbox(chi_pt, anc_ct)){
                        isAncestor = true;
                    }
                }
            }else if(ancestor instanceof NullType) {
                isAncestor = false;
            }else if(ancestor instanceof BottomType){
                //todo: [zw] check later BottomType = NullType?
                if(child instanceof NullType){
                    isAncestor = true;
                }
            }else if(child instanceof NullType || child instanceof BottomType){
                isAncestor = false;
            }else if (child instanceof ClassType && ancestor instanceof ClassType){
                isAncestor = canStoreType((ClassType) ancestor, (ClassType) child);
            }else if(child instanceof ArrayType && ancestor instanceof ClassType){
                //todo:[zw] check WeakObject
                String name = ((ClassType) ancestor).getFullyQualifiedName();
                if(name.equals("java.lang.Object") || name.equals("java.lang.Cloneable") || name.equals("java.io.Serializable")){
                    isAncestor = true;
                }
            }else if(child instanceof ArrayType && ancestor instanceof ArrayType){
                ArrayType anArr = (ArrayType) ancestor;
                ArrayType chArr = (ArrayType) child;
                if(anArr.getDimension() == chArr.getDimension()){
                    Type anBase = anArr.getBaseType();
                    Type chBase = chArr.getBaseType();
                    if(anBase.equals(chBase)){
                        isAncestor = true;
                    }else if(anBase instanceof ClassType && chBase instanceof ClassType){
                        isAncestor = canStoreType((ClassType) anBase, (ClassType)  chBase);
                    }else {
                        isAncestor = false;
                    }
                }else if(anArr.getDimension() < chArr.getDimension()){
                    if(anArr.getBaseType() instanceof ClassType){
                        //todo:[zw] check WeakObject
                        String name = ((ClassType) anArr.getBaseType()).getFullyQualifiedName();
                        if(name.equals("java.lang.Object") || name.equals("java.lang.Cloneable") || name.equals("java.io.Serializable")){
                            isAncestor = true;
                        }
                    }
                }
            }
        }
        return isAncestor;
    }

    public Collection<Type> getLeastCommonAncestor (Type a, Type b, boolean useWeakObjectType){

        if(PrimitiveHierarchy.arePrimitives(a,b)){
            return PrimitiveHierarchy.getLeastCommonAncestor(a, b);
        }else if(a instanceof BottomType) {
            return Collections.singleton(b);
        }else if(b instanceof BottomType){
            return Collections.singleton(a);
        }else if(a instanceof NullType){
            return Collections.singleton(b);
        }else if(b instanceof NullType){
            return Collections.singleton(a);
        }else if(isAncestor(a, b)){
            return Collections.singleton(a);
        }else if(isAncestor(b, a)){
            return Collections.singleton(b);
        }else if(a instanceof ClassType && b instanceof ClassType){

        }

        return Collections.emptySet();
    }

    private boolean isBoxOrUnbox(ClassType a, PrimitiveType b){
        if(a.getFullyQualifiedName().equals("java.lang.Object")){
            return true;
        }else if(a.getFullyQualifiedName().equals("java.io.Serializable")){
            return b instanceof IntegerType || b instanceof PrimitiveType.FloatType || b instanceof PrimitiveType.DoubleType || b instanceof PrimitiveType.LongType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Boolean")){
            return b instanceof PrimitiveType.BooleanType || b instanceof PrimitiveType.Integer1Type;
        }else if (a.getFullyQualifiedName().equals("java.lang.Byte")){
            return b instanceof PrimitiveType.ByteType || b instanceof PrimitiveType.Integer1Type || b instanceof PrimitiveType.Integer127Type;
        }else if (a.getFullyQualifiedName().equals("java.lang.Short")){
            return b instanceof PrimitiveType.ShortType || b instanceof PrimitiveType.Integer1Type || b instanceof PrimitiveType.Integer127Type || b instanceof PrimitiveType.Integer32767Type;
        }else if(a.getFullyQualifiedName().equals("java.lang.Character")){
            return b instanceof PrimitiveType.CharType || b instanceof PrimitiveType.Integer1Type || b instanceof PrimitiveType.Integer127Type || b instanceof PrimitiveType.Integer32767Type;
        }else if(a.getFullyQualifiedName().equals("java.lang.Integer")){
            return b instanceof PrimitiveType.IntType || b instanceof PrimitiveType.Integer1Type || b instanceof PrimitiveType.Integer127Type || b instanceof PrimitiveType.Integer32767Type;
        }else if(a.getFullyQualifiedName().equals("java.lang.Long")){
            return b instanceof PrimitiveType.LongType || b instanceof PrimitiveType.Integer1Type || b instanceof PrimitiveType.Integer127Type || b instanceof PrimitiveType.Integer32767Type;
        }else if(a.getFullyQualifiedName().equals("java.lang.Double")){
            return b instanceof PrimitiveType.DoubleType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Float")){
            return b instanceof PrimitiveType.FloatType;
        }else{
            return false;
        }
    }

    private boolean canStoreType(ClassType ancestor, ClassType child){
        if(ancestor.getFullyQualifiedName().equals("java.lang.Object")){
            return true;
        }else if(typeHierarchy.isInterface(child)){
            if(typeHierarchy.isInterface(ancestor)){
                return typeHierarchy.subtypesOf(ancestor).contains(child);
            }else {
                return false;
            }
        }else if(typeHierarchy.isClass(child)){
             return typeHierarchy.subtypesOf(ancestor).contains(child);
        }else{
            return false;
        }
    }

    private List<AncestryPath> buildAncestryPaths(ClassType type){
        Deque<AncestryPath> pathNodes = new ArrayDeque<>();
        pathNodes.add(new AncestryPath(type, null));
        Set<AncestryPath> paths = new HashSet<>();
        while(!pathNodes.isEmpty()){
            AncestryPath node = pathNodes.removeFirst();
            if(node.type.getFullyQualifiedName().equals("java.lang.Object")){
                paths.add(node);
            }else{
                if(typeHierarchy.isInterface(node.type)){
                    if(typeHierarchy.isInterface()){

                    }else{

                    }
                }else{

                }
            }
        }

    }

    private class AncestryPath{
        public AncestryPath next;
        public ClassType type;

        public AncestryPath(@Nonnull ClassType type, @Nullable AncestryPath next){
            this.type = type;
            this.next = next;
        }
    }

}
