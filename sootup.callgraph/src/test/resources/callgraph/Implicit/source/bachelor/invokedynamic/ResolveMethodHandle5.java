package bachelor.invokedynamic;

import java.util.Arrays;
import java.util.List;

interface ListParam {
    void sayHello();
}

public class ResolveMethodHandle5 {
    public void method1(List<?> list) {
        ListParam g = () -> System.out.println(list);
        g.sayHello();
    }

    public static void main(String[] args) {
        ResolveMethodHandle5 r = new ResolveMethodHandle5();
        List<?> stringList = Arrays.asList("a", "b", "c");
        r.method1(stringList);
    }
}