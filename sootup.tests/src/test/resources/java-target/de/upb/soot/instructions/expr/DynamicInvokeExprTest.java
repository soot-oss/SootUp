package de.upb.sootup.instructions.expr;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class DynamicInvokeExprTest {

  String sth() {
    return "something";
  }

  void invoke() throws Throwable {

    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodType methodType = MethodType.methodType(String.class);
    MethodHandle methodHandle = lookup.findVirtual(DynamicInvokeExprTest.class, "sth", methodType);
    String result = (String) methodHandle.invokeExact(new DynamicInvokeExprTest());
    System.out.println("dynamic invoked method returned: " + result);

  }
}
