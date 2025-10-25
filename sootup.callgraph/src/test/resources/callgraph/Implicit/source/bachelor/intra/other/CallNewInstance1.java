package bachelor.intra.other;

import java.lang.reflect.Constructor;

public class CallNewInstance1 {
    public static class MyClass {
        public MyClass() {
        }
    }

    public static void main(String[] args) throws Exception {
        Constructor<MyClass> ctor = MyClass.class.getConstructor();
        MyClass instance = ctor.newInstance();
    }
}
