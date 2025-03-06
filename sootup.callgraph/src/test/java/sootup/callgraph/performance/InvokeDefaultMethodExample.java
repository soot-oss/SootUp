package sootup.callgraph.performance;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class InvokeDefaultMethodExample {
  public static void main(String[] args) throws Throwable {
    OverrideDefaultMethod override = new OverrideDefaultMethod();
    // overwritten method override.defaultMeth();
    MethodHandle handle =
        MethodHandles.lookup()
            .findSpecial(
                InvokeDefaultMethod.class,
                "defaultMeth",
                MethodType.methodType(void.class),
                OverrideDefaultMethod.class);
    handle.invoke(override);
  }
}
