import java.io.IOException;

public class ICFGCallExceptionsExample {

    static String field = "";

    public static void main(String[] args) {
        callsNeverThrows();
        callsThrowsIse();
        callsNested();
        callsRecursive();
        callsCatchesItself();
        callsReadsArray(args);
        callsNativeWithoutThrows();
        callsNativeWithThrows();
        callsInstanceOnNull(null);
    }

    static void callsNeverThrows() {
        try {
            neverThrows();
        } catch (ArrayIndexOutOfBoundsException e) {
            field = "unreachable";
        }
    }

    static void callsThrowsIse() {
        try {
            throwsIse();
        } catch (IllegalStateException e) {
            field = "reachable";
        }
    }

    static void callsNested() {
        try {
            nested();
        } catch (IllegalStateException e) {
            field = "reachable";
        }
    }

    static void callsRecursive() {
        try {
            recursive(3);
        } catch (IllegalStateException e) {
            field = "reachable";
        }
    }

    static void callsCatchesItself() {
        try {
            catchesItself();
        } catch (IllegalStateException e) {
            field = "unreachable";
        }
    }

    static void callsReadsArray(String[] arr) {
        try {
            readsArray(arr);
        } catch (ArrayIndexOutOfBoundsException e) {
            field = "reachable";
        }
    }

    static void callsNativeWithoutThrows() {
        try {
            nativeWithoutThrows();
        } catch (ArrayIndexOutOfBoundsException e) {
            field = "unreachable";
        }
    }

    static void callsNativeWithThrows() {
        try {
            nativeWithThrows();
        } catch (IOException e) {
            field = "reachable";
        }
    }

    static void callsInstanceOnNull(ICFGCallExceptionsExample o) {
        try {
            o.instanceNeverThrows(); // throws a NullPointerException at the call if o is null
        } catch (NullPointerException e) {
            field = "reachable";
        }
    }

    void instanceNeverThrows() {
        field = "x";
    }

    static void neverThrows() {
        field = "x";
    }

    static void throwsIse() {
        field = "x";
        throw new IllegalStateException("thrown after the field was written");
    }

    static void nested() {
        throwsIse();
    }

    static void recursive(int n) {
        if (n > 0) {
            recursive(n - 1);
        } else {
            throw new IllegalStateException();
        }
    }

    static void catchesItself() {
        try {
            throwsIse();
        } catch (IllegalStateException e) {
            field = "caught";
        }
    }

    static String readsArray(String[] arr) {
        return arr[0];
    }

    static native void nativeWithoutThrows();

    static native void nativeWithThrows() throws IOException;
}
