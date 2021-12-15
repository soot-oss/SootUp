package propagator;

import benchmark.objects.B;
import benchmark.objects.O;

public class AliasProp {

    public void test() {

        boolean condition = true;
        O o1 = new O(); //r1 = o1;
        O o2 = new O(); //r2 = o2
        B b = new B(); //r3 = b;

        O p, q;
        if(condition){
            p = o1;
        }else{
            p = o2;
        }
        q = o1;
        p.f = b; //p = u0;
        B s = q.f; //q = u1;
    }
}
