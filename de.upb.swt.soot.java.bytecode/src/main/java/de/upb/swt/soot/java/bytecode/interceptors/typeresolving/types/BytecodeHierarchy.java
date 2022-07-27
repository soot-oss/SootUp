package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.PrimitiveHierarchy;

import java.util.Collection;
import java.util.Collections;

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
            }else if(child instanceof NullType || child instanceof BottomType){
                isAncestor = true;
            }else if(ancestor instanceof NullType || ancestor instanceof BottomType){
                isAncestor = false;
            }
        }

        //todo later delete
        return isAncestor;
    }

    public Collection<Type> getLeastCommonAncestor (Type a, Type b, boolean useWeakObjectType){
        return Collections.emptySet();
    }

    public boolean isBoxOrUnbox(ClassType a, PrimitiveType b){
        if(a.getFullyQualifiedName().equals("java.lang.Byte")){
            return b instanceof PrimitiveType.DoubleType;
        }else if (a.getFullyQualifiedName().equals("java.lang.Short")){
            return b instanceof PrimitiveType.ShortType;
        }else if (a.getFullyQualifiedName().equals("java.lang.Integer")){
            return b instanceof PrimitiveType.IntType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Long")){
            return b instanceof PrimitiveType.LongType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Character")){
            return b instanceof PrimitiveType.CharType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Float")){
            return b instanceof PrimitiveType.FloatType;
        }else if(a.getFullyQualifiedName().equals("java.lang.Double")){
            return b instanceof PrimitiveType.DoubleType;
        }else{
            return false;
        }
    }



}
