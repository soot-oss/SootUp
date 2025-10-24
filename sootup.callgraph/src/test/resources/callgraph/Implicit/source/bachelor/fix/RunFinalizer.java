package bachelor.fix;

public class RunFinalizer {
    static class DemoObject {
        @Override
        protected void finalize() {
        }
    }

    public static void main(String[] args) {
        new DemoObject();
        System.gc();
        Runtime.getRuntime().runFinalization();
    }
}
