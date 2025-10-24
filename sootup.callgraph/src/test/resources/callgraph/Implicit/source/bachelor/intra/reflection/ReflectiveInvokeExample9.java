// multiple methods with the targetMethodName + only in superClass
package bachelor.intra.reflection;

import java.lang.reflect.Method;

public class ReflectiveInvokeExample9 {

    static class Base {
        public void targetMethod() {
        }
        public void targetMethod(String msg) {
        }
    }

    static class Target extends Base {
    }

    public static void main(String[] args) throws Exception {
        Target target = new Target();
        Method noArgs = Target.class.getMethod("targetMethod");
        noArgs.invoke(target);
    }
}
