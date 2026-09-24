package pkgb1;

public class B1 {
    public String doIt(String input) {
        return "from B.doIt, " + input;
    }

    @SuppressWarnings("unused")
    private String doItPrivate(String input) {
        return "from B.doItPrivate, " + input;
    }

    @SuppressWarnings("unused")
    private static String doItPrivateStatic(String input) {
        return "from B.doItPrivateStatic, static, " + input;
    }
}
