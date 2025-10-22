package pkgb;

public class MyException extends Exception {
    private static final long serialVersionUID = 1L;

    public MyException() {
        super("MyException's message");
    }
}
