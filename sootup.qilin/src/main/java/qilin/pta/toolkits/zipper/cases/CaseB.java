package qilin.pta.toolkits.zipper.cases;

// No Out, case 2
public class CaseB {
    static class A {
        Object f;

        void bar(Object q) {
            this.f = q;
        }
    }

    void foo() {
        Object oy = new Object();
        Object oz = new Object();

        A a = new A();
        a.bar(oy);
        Object o = a.f;

//        A a1 = new A();
//        a1.bar(oz);
//        Object o1 = a1.f;
    }
}
