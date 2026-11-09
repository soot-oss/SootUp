package pkgbinternal;

public class MyInternalRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public MyInternalRuntimeException() {
        super("MyInternalRuntimeException's message");
    }
}
