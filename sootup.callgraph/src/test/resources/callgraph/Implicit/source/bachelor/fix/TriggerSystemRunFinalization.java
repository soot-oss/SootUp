package bachelor.fix;

public class TriggerSystemRunFinalization {
    static class DemoObject {
        @Override
        protected void finalize() {
        }
    }

    public static void main(String[] args) {
        new DemoObject();
        System.gc();
        System.runFinalization();
    }
}
