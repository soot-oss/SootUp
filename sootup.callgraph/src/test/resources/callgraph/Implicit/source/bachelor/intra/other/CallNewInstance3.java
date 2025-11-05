package bachelor.intra.other;

import java.lang.reflect.Constructor;

public class CallNewInstance3 {
    public static class MyClass {
        private int number;
        private String name;
        private boolean bool;

        public MyClass(int number, String name, boolean bool) {
            this.number = number;
            this.name = name;
            this.bool = bool;
        }
    }

    public static void main(String[] args) throws Exception {
        Constructor<MyClass> ctor = MyClass.class.getConstructor(int.class, String.class, boolean.class);
        MyClass instance = ctor.newInstance(5, "THE END", true);
    }
}
