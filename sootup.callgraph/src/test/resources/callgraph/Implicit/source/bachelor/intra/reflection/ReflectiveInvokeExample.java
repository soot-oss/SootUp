// targetMethodName is overwritten in subClass -> only edge to Target.targetMethod
package bachelor.intra.reflection;

import java.lang.reflect.Method;

public class ReflectiveInvokeExample {

    static class Base {
        public void targetMethod() {}
    }

    static class Target extends Base {
        @Override
        public void targetMethod() {}
    }

    public static void main(String[] args) throws Exception {
        Target target = new Target();

        Method m = Target.class.getMethod("targetMethod");
        m.invoke(target);
    }
}
