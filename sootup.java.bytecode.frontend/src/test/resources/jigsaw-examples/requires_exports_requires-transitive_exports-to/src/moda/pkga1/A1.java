package pkga1;

import pkgb.B;
import pkgc.C;

public class A1 {
    public String doIt() {
        return "from A1, " + new B().doIt();
    }

    public C getMyC() {
        return new C();
    }
}
