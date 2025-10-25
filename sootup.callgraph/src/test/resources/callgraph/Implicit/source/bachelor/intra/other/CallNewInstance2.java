package bachelor.intra.other;

import java.lang.reflect.Constructor;

public class CallNewInstance2 {
    public static class MyClass {
        private int number;

        public MyClass() {
        }

        public MyClass(int number) {
            this.number = number;
        }
    }

    public static void main(String[] args) throws Exception {
        Constructor<MyClass> ctor = MyClass.class.getConstructor(int.class);
        MyClass instance = ctor.newInstance(5);
    }
}
