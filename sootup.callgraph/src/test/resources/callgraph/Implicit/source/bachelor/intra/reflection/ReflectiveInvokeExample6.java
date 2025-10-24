package bachelor.intra.reflection;

import java.lang.reflect.Method;

public class ReflectiveInvokeExample6 {
    static class T { public void x(){} }

    public static void main(String[] args) throws Exception {
        T t = new T();
        // array literal created inline, then indexed — no named string local
        Method m = T.class.getMethod(new String[] { "x" }[0]);
        m.invoke(t);
    }
}
