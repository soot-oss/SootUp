
public class MissingTrapHandlerAssignment{

    static Object a;
    public long run() {
        synchronized(a) {
            try {
                return 1;
            } catch (Throwable t) {
                synchronized (this) {
                    if (a != null) {
                        try {
                            a.toString();
                        } catch (Throwable t1) {
                            t.addSuppressed(t1);
                        }
                    }
                }
                throw t;
            }
        }
    }
}