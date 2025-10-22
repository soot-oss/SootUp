package pkgbinternal;

class InternalB {
    public String doIt(String input) {
        return "from InternalB.doIt, " + input;
    }

    @SuppressWarnings("unused")
    private String doItPrivate(String input) {
        return "from InternalB.doItPrivate, " + input;
    }

    @SuppressWarnings("unused")
    private static String doItPrivateStatic(String input) {
        return "from InternalB.doItPrivateStatic, static, " + input;
    }
}
