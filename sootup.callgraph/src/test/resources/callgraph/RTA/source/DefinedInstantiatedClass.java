package dic;

interface InterfaceA {
    void sound();
}

class ClassB implements InterfaceA {
    public void sound() {
    }
}

class ClassC {
    public void play(InterfaceA a) {
        if (a != null) {
            a.sound();
        }
    }
}

class DefinedInstantiatedClass {
    public static void main(String[] args) {
        ClassC c = new ClassC();
        c.play(null);
    }
}
