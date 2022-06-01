package de.upb.swt.soot.core;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.types.ClassType;
import de.upb.swt.soot.core.types.NullType;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.core.types.Type;
import de.upb.swt.soot.core.views.View;

import javax.annotation.Nonnull;

public class HierarchyUtils {

    @Nonnull
    private final View<? extends SootClass> view;



    public HierarchyUtils(View<SootClass> view){
        this.view = view;
    }

    /**
     * Check if variable with child type could be stored into/assigned to another variable with parent type
     * @return if parent p, child c, and p = c, then return true, otherwise return false
     */
    public boolean canStoreType(Type parent, Type child){
       if(parent.equals(child)){
           return true;
       }else if(parent instanceof NullType){
           return false;
       }else if(child instanceof NullType){
           return parent instanceof ReferenceType;
       }else if(child instanceof ClassType){
           if(parent.toString().equals("java.lang.Object")){
               return true;
           }else if(parent instanceof ClassType){

           }
        }
       //later delete
       return false;
    }

    public boolean canStoreClass(SootClass parent, SootClass child){
        return false;
    }
}
