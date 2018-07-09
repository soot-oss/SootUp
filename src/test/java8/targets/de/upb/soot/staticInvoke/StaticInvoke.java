package de.upb.soot.staticInvoke;

import java.lang.reflect.*;

public class StaticInvoke extends A {
    public static void main(String [] args) throws NoSuchMethodException {
        StaticInvoke s = new StaticInvoke();
        s.static_invoke();
        s.static_invoke_reflection();
    }
    public static void static_invoke() throws NoSuchMethodException {
        // Invoking static method
        A.methodA();
    }
    public static void static_invoke_reflection() throws NoSuchMethodException {
        A a = new A();
        // Invoking static method using reflection
        Method method = a.getClass().getMethod("methodA");
        try {
            method.invoke(a);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}