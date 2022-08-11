package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.model.SootClass;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.core.typerhierachy.ViewTypeHierarchy;
import de.upb.swt.soot.core.types.*;
import de.upb.swt.soot.core.views.View;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.IHierarchy;
import de.upb.swt.soot.java.bytecode.interceptors.typeresolving.PrimitiveHierarchy;
import de.upb.swt.soot.java.core.types.JavaClassType;

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
        if(!isAncestor && !(PrimitiveHierarchy.arePrimitives(ancestor, child))){
            if(ancestor.equals(child)){
                isAncestor = true;
            // if one of them is PrimitiveType, and another one is ReferenceType
            } else if(ancestor instanceof PrimitiveType || child instanceof PrimitiveType){
                PrimitiveType prim = (PrimitiveType) ((ancestor instanceof PrimitiveType) ? ancestor : child);
                ReferenceType ref = (ReferenceType) ((ancestor instanceof PrimitiveType) ? child : ancestor);
                isAncestor = isBoxOrUnbox(ref, prim);
            } else if(ancestor instanceof NullType){
                isAncestor = false;
            }else if(ancestor instanceof BottomType){
                //todo: [zw] check later BottomType = NullType?
                if(child instanceof NullType){
                    isAncestor = true;
                }
            }else if(child instanceof NullType || child instanceof BottomType){
                isAncestor = true;
            }else if (child instanceof ClassType && ancestor instanceof ClassType){
                isAncestor = canStoreType((ClassType) ancestor, (ClassType) child);
            }else if(child instanceof ArrayType && ancestor instanceof ClassType){
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
                    }
                }else if(anArr.getDimension() < chArr.getDimension()){
                    if(anArr.getBaseType() instanceof ClassType){
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

    public Collection<Type> getLeastCommonAncestor (Type a, Type b){
        Collection<Type> ret = new HashSet<>();
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
        }else if(a instanceof ArrayType && b instanceof ArrayType){
            Collection<Type> temp;
            Type base_a = ((ArrayType) a).getBaseType();
            Type base_b = ((ArrayType) b).getBaseType();
            int dim_a = ((ArrayType) a).getDimension();
            int dim_b = ((ArrayType) b).getDimension();
            if(base_a instanceof PrimitiveType || base_b instanceof PrimitiveType){
                temp = Collections.emptySet();
            }else if(dim_a != dim_b){
                temp = Collections.emptySet();
            }else{
                temp = getLeastCommonAncestor(base_a, base_b);
            }
            if(temp.isEmpty()){
                ret.add(new JavaClassType("Serializable", new PackageName("java.io")));
                ret.add(new JavaClassType("Cloneable", new PackageName("java.lang")));
            }
        }else if(a instanceof ArrayType || b instanceof ArrayType){
            ClassType nonArray = (ClassType) ((a instanceof ArrayType) ? b : a);
            if(!nonArray.getFullyQualifiedName().equals("java.lang.Object")){
                Type ser = new JavaClassType("Serializable", new PackageName("java.io"));
                Type clo = new JavaClassType("Cloneable", new PackageName("java.lang"));
                if(isAncestor(ser, nonArray)){
                    ret.add(ser);
                }
                if(isAncestor(clo, nonArray)){
                    ret.add(clo);
                }
            }
            if(ret.isEmpty()){
                ret.add(new JavaClassType("Object", new PackageName("java.lang")));
            }
        }else{
            //if a and b are both ClassType
            Set<AncestryPath> pathsA = buildAncestryPaths((ClassType) a);
            Set<AncestryPath> pathsB = buildAncestryPaths((ClassType) b);
            for(AncestryPath pathA : pathsA){
                for(AncestryPath pathB : pathsB){
                    ClassType lcn = leastCommonNode(pathA, pathB);
                    boolean isLcn = true;
                    for(Type l : ret){
                        if(isAncestor(lcn, l)){
                            isLcn = false;
                            break;
                        }
                        if(isAncestor(l, lcn)){
                            ret.remove(l);
                        }
                    }
                    if(isLcn){
                        ret.add(lcn);
                    }
                }
            }
            if(ret.isEmpty()){
                ret.add(new JavaClassType("Object", new PackageName("java.lang")));
            }
        }
        return ret;
    }

    private boolean isBoxOrUnbox(ReferenceType a, PrimitiveType b){
        boolean ret;
        if(a instanceof ArrayType || a instanceof NullType){
            return false;
        }
        String name = ((ClassType) a).getFullyQualifiedName();
        switch (name){
            case "java.lang.Boolean":
                ret =  b instanceof PrimitiveType.BooleanType;
                break;
            case "java.lang.Byte":
                ret =  b instanceof PrimitiveType.ByteType;
                break;
            case "java.lang.Short":
                ret =  b instanceof PrimitiveType.ShortType;
                break;
            case "java.lang.Character":
                ret =  b instanceof PrimitiveType.CharType;
                break;
            case "java.lang.Integer":
                ret =  b instanceof PrimitiveType.IntType;
                break;
            case "java.lang.Long":
                ret =  b instanceof PrimitiveType.LongType;
                break;
            case "java.lang.Float":
                ret =  b instanceof PrimitiveType.FloatType;
                break;
            case "java.lang.Double":
                ret =  b instanceof PrimitiveType.DoubleType;
                break;
            default:
                ret = false;
        }
        return ret;
    }

    private boolean canStoreType(ClassType ancestor, ClassType child){
        if(ancestor.getFullyQualifiedName().equals("java.lang.Object")){
            return true;
        }else {
            return typeHierarchy.subtypesOf(ancestor).contains(child);
        }
    }

    private Set<AncestryPath> buildAncestryPaths(ClassType type){
        Deque<AncestryPath> pathNodes = new ArrayDeque<>();
        pathNodes.add(new AncestryPath(type, null));
        Set<AncestryPath> paths = new HashSet<>();
        while(!pathNodes.isEmpty()){
            AncestryPath node = pathNodes.removeFirst();
            if(node.type.getFullyQualifiedName().equals("java.lang.Object")){
                paths.add(node);
            }else{
                if(typeHierarchy.isInterface(node.type)){
                    Set<ClassType> superInterfaces = typeHierarchy.directlyExtendedInterfacesOf(node.type);
                    if(superInterfaces.isEmpty()){
                        JavaClassType objectType = new JavaClassType("Object", new PackageName("java.lang"));
                        AncestryPath root = new AncestryPath(objectType, node);
                        paths.add(root);
                    }else{
                        for(ClassType superInterface : superInterfaces){
                            AncestryPath superNode = new AncestryPath(superInterface, node);
                            pathNodes.add(superNode);
                        }
                    }
                }else{
                    Set<ClassType> superInterfaces = typeHierarchy.directlyImplementedInterfacesOf(node.type);
                    for(ClassType superInterface : superInterfaces){
                        AncestryPath superNode = new AncestryPath(superInterface, node);
                        pathNodes.add(superNode);
                    }
                    ClassType superClass = typeHierarchy.directlySuperClassOf(node.type);
                    AncestryPath superNode = new AncestryPath(superClass, node);
                    pathNodes.add(superNode);
                }
            }
        }
        return paths;
    }

    @Nullable
    private ClassType leastCommonNode(AncestryPath a, AncestryPath b){
        ClassType lcn = null;
        while(a != null && b != null && a.type.equals(b.type) ){
            lcn = a.type;
            a = a.next;
            b = b.next;
        }
        return lcn;
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
