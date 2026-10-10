public class ICFGBodylessCalleeExample {

    public static void main(String[] args) {
        int a = nativeCall(1);
        int b = withBody(a);
    }

    static native int nativeCall(int x);

    static int withBody(int x) {
        return x;
    }
}
