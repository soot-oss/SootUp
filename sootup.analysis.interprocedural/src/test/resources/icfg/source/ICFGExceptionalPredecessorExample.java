public class ICFGExceptionalPredecessorExample {

    public static void main(String[] args) {
        overwriteInTry(args, "secret");
    }

    static String overwriteInTry(String[] arr, String secret) {
        String x = secret;
        try {
            x = arr[0]; // may throw before x is overwritten
        } catch (RuntimeException e) {
            return x; // x may still be the secret
        }
        return x;
    }
}
