package de.upb.soot.staticInvoke;

import java.lang.reflect.*;

public class StaticInvoke extends A {

    public static void staticInvoke() throws NoSuchMethodException {
        // Invoking static method
        A.methodA();
    }
    public static void staticInvokeReflection() throws NoSuchMethodException {
        // Invoking static method using reflection
        Method method = A.class.getClass().getMethod("methodA");
        try {
            method.invoke(method);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}