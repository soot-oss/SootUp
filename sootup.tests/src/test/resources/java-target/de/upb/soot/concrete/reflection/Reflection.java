package de.upb.sootup.concrete.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Manuel Benz created on 21.07.18
 */
public class Reflection {

  public static void staticInvokeReflection() throws NoSuchMethodException {
    // Invoking static method using reflection
    Method method = A.class.getMethod("staticFoo");
    try {
      method.invoke(null);
    } catch (IllegalAccessException e) {
      e.printStackTrace();
    } catch (InvocationTargetException e) {
      e.printStackTrace();
    }
  }
}
