package pkgb;

public class MyRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MyRuntimeException() {
        super("MyRuntimeException's message");
    }
}
