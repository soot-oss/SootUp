package de.upb.swt.soot.java.bytecode.interceptors.typeresolving.types;

import de.upb.swt.soot.core.jimple.visitor.TypeVisitor;
import de.upb.swt.soot.core.signatures.PackageName;
import de.upb.swt.soot.java.core.types.JavaClassType;

import javax.annotation.Nonnull;

/**
 * This type is used for Type Inference.
 * Object, Serializable, Cloneable are weak object types.
 *
 * @author Zun Wang
 */
public class WeakObjectType extends JavaClassType {

    public WeakObjectType(String className, PackageName packageName){
        super(className, packageName);
        if(className.equals("Object") || className.equals("Cloneable")){
            if(!packageName.toString().equals("java.lang")){
                throw new RuntimeException(this + " is not an object with WeakObjectType");
            }
        } else if(className.equals("Serializabel")){
            if(!packageName.toString().equals("java.io")){
                throw new RuntimeException(this + " is not an object with WeakObjectType");
            }
        }else {
            throw new RuntimeException(this + " is not an object with WeakObjectType");
        }
    }

    @Override
    public void accept(@Nonnull TypeVisitor v) {
        //todo: weak objects type case
    }

}
