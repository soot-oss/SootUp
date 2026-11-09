package tfx;

import java.lang.reflect.Proxy;

/** Proxy -> handler -> Method.invoke -> RealWorker.work. */
public class ProxyForward {
  public static void main(String[] args) {
    Worker w =
        (Worker)
            Proxy.newProxyInstance(
                Worker.class.getClassLoader(),
                new Class<?>[] {Worker.class},
                new ForwardingHandler(new RealWorker()));
    w.work("job");
  }
}
