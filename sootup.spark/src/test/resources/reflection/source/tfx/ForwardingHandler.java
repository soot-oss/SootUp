package tfx;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/** Dynamic-proxy handler forwarding reflectively - invoke site in an instance method. */
public class ForwardingHandler implements InvocationHandler {
  private final Object target;

  public ForwardingHandler(Object target) {
    this.target = target;
  }

  public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
    return m.invoke(target, args);
  }
}
