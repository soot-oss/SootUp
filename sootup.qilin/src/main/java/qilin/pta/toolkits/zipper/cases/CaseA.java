package qilin.pta.toolkits.zipper.cases;

// NO IN , NO OUT, CASE 4.
public class CaseA {
    static class A {
        Object f;
    }

    A create() {
        return new A();
    }

    void foo() {
        Object o = new Object();
        A a = create();
        a.f = o;
        o = a.f;
    }
}
