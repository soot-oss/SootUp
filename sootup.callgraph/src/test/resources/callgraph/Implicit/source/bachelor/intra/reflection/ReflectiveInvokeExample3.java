package bachelor.intra.reflection;

import java.lang.reflect.Method;

public class ReflectiveInvokeExample3 {
    static class T { public void a(){} public void b(){} }

    public static void main(String[] args) throws Exception {
        T t = new T();
        Method m = T.class.getMethod(args.length > 0 ? "a" : "b");
        m.invoke(t);
    }
}
