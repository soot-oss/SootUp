package qilin.pta.toolkits.zipper.cases;

public class CaseC {
    static class A {
        Object f;
    }

    A f;

    // case 2;
    CaseC() {
        A a = new A();
        this.f = a;
    }

    // does not consider this_variable.
    A getF() {
        A r = this.f;
        return r;
    }

    void foo() {
        Object o1 = new Object();
        CaseC c1 = new CaseC();
        A a1 = c1.getF();
        a1.f = o1;
        Object o1x = a1.f;

//        Object o2 = new Object();
//        CaseC c2 = new CaseC();
//        A a2 = c2.getF();
//        a2.f = o2;
//        Object o2x = a2.f;
    }
}
