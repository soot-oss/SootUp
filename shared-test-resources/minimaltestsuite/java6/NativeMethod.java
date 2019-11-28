/** @author Kaustubh Kelkar */
public class NativeMethod {
        public native int intMethod(int i);
        public static void main(String[] args) {
                System.loadLibrary("Main");
                System.out.println(new NativeMethod().intMethod(2));
        }
}