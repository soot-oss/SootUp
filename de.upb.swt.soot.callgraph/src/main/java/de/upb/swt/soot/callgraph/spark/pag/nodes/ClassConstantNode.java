package de.upb.swt.soot.callgraph.spark.pag.nodes;

import de.upb.swt.soot.core.jimple.common.constant.ClassConstant;
import de.upb.swt.soot.core.types.ReferenceType;
import de.upb.swt.soot.java.core.JavaIdentifierFactory;
import de.upb.swt.soot.java.core.types.JavaClassType;
import sun.jvm.hotspot.debugger.cdbg.RefType;

/**
 * Represents an allocation site node the represents a known java.lang.Class object.
 */
public class ClassConstantNode extends AllocationNode {
    public String toString(){
        return "ClassConstantNode " + getNewExpr();
    }

    public ClassConstant getClassConstant(){
        return (ClassConstant) getNewExpr();
    }

    public ClassConstantNode(ClassConstant cc){
        super(new JavaClassType("Class", JavaIdentifierFactory.getInstance().getPackageName("java.lang")), cc, null);
    }

}
