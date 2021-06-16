package pkgmain;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class Main {
    public static void main(String[] args) throws Exception {
// access to pkga1 (exported from moda)
        pkga1.A1 mya1 = new pkga1.A1();
        System.out.println("A1: " + mya1.doIt());

        Object myc1 = mya1.getMyC();
        System.out.println("from A1: getMyC()=" + myc1);

        pkgc.C myc2 = mya1.getMyC();
        System.out.println("from A1: getMyC()=" + myc2);

        // --------------------------------------------------------------------------------------------
        
// access to pkga2 (exported from moda only to modmain)
        pkga2.A2 mya2 = new pkga2.A2();
        System.out.println("A2: " + mya2.doIt());

        // --------------------------------------------------------------------------------------------

// does not compile, access to pkgainternal not possible as not exported
//        pkgainternal.InternalA myinternalA = new pkgainternal.InternalA();

        // --------------------------------------------------------------------------------------------

// does not compile, access to pkga3 only possible via reflection
//       pkga3.A3 mya3 = new A3();
        
// access to pkga3 (exported dynamic from moda, hence only useable via reflection)
        Class<?> myA3Class = Class.forName("pkga3.A3");
        Constructor<?> con = myA3Class.getDeclaredConstructor();
        con.setAccessible(true);
        Object myA3 = con.newInstance();

        Method m = myA3.getClass().getMethod("doIt");
        m.setAccessible(true);
        System.out.println("A3: " + m.invoke(myA3));
    }
}
