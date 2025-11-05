package bachelor.invokedynamic;

interface StringParam {
    void sayHello();
}

public class ResolveMethodHandle4 {
    public void method1(String str) {
        StringParam g = () -> System.out.println(str);
        g.sayHello();
    }

    public static void main(String[] args) {
        ResolveMethodHandle4 r = new ResolveMethodHandle4();
        r.method1("Hello");
    }
}