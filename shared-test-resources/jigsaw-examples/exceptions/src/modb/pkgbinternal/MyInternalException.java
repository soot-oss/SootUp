package pkgbinternal;

public class MyInternalException extends Exception {
    private static final long serialVersionUID = 1L;

    public MyInternalException() {
        super("MyInternalException's message");
    }
}
