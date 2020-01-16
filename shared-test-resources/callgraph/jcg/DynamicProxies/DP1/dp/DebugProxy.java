package dp;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
public class DebugProxy implements InvocationHandler {
    private Object obj;
    public static Object newInstance(Object obj) {
        return Proxy.newProxyInstance(
        obj.getClass().getClassLoader(),obj.getClass().getInterfaces(),
        new DebugProxy(obj));
    }
    private DebugProxy(Object obj) { this.obj = obj; }
    public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
        System.out.println("before method " + m.getName());
        return m.invoke(obj, args);
    }
}
