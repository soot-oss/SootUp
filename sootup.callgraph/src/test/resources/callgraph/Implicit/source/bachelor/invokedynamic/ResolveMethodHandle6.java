// test if converted binary does not mess with other
package bachelor.invokedynamic;

import java.lang.reflect.Method;

public class ResolveMethodHandle6 {
    static class Target {
        public void target() {

        }

        public void other() {

        }
    }

    public static void main(String[] args) throws Exception {
        Target target = new Target();

        // local whose value determines the reflected target method name
        String part;
        if (args.length > 0) {
            // branch A -> the intended target
            part = "target";
        } else {
            // branch B -> a different method name
            part = "other";
        }

        Method m = Target.class.getMethod(part, String.class);
        m.invoke(target);

        ResolveMethodHandle4 r = new ResolveMethodHandle4();
        r.method1("Hello");
    }
}
