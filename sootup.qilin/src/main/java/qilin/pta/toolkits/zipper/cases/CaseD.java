package qilin.pta.toolkits.zipper.cases;

public class CaseD {
    static class A {
        Object f, g;
    }

    Object id(Object p) {
        A a = new A();
        a.f = p;
        Object r = a.f;
        return r;
    }

    Object fakeId(Object p) {
        A a = new A();
        a.f = p;
        a.g = new Object();
        return a.g;
    }
}
