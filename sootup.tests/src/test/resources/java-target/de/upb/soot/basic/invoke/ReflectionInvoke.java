package de.upb.sootup.basic.invoke;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionInvoke extends A {

  public static void ReflectionInvoke() throws NoSuchMethodException {
    // Invoking static method
    A.methodA();
  }

  public static void staticInvokeReflection() throws NoSuchMethodException {
    // Invoking static method using reflection ( so its virtualinvoke)
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
