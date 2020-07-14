package de.upb.swt.soot.java.bytecode.interceptors;

import de.upb.swt.soot.core.jimple.basic.Value;

public class Evaluator {

    public static boolean isValueConstantValued(Value op){
        return true;
    }

    public static Value getConstantValueOf(Value op){
        return op;
    }
}
