package propagator;

import benchmark.objects.B;
import benchmark.objects.O;

public class AliasProp {

    public static void  main(String [] args) {
        boolean condition = true;
        O o1 = new O();
        O o2 = new O();
        B b = new B();

        O p;
        if(condition){
            p = o1;
        }else{
            p = o2;
        }
        p.f = b;
        B s = o1.f;
    }
}
