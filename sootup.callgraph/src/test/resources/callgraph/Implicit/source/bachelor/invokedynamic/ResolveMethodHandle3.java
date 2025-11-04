package bachelor.invokedynamic;

interface Greeting {
    void sayHello();
}

public class ResolveMethodHandle3 {
    public void method1() {
        Greeting g = () -> System.out.println("Hello from lambda");
        g.sayHello();
    }

    public static void main(String[] args) {
        ResolveMethodHandle3 r = new ResolveMethodHandle3();
        r.method1();
    }
}