package bachelor.invokedynamic;

public class ResolveMethodHandle1 {
    public void m1() {
        Runnable runnable = this::m2;
        runnable.run();
    }
    public void m2() {
    }
    public static void main(String[] args) {
        ResolveMethodHandle1 m = new ResolveMethodHandle1();
        m.m1();
    }
}