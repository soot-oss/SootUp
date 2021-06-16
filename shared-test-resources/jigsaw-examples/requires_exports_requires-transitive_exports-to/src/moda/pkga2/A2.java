package pkga2;

import pkgainternal.InternalA;

public class A2 {
    public String doIt() {
        return "from A2 (plus: " + new InternalA().doIt() + ")";
    }
}
