package pkgb;

import pkgc.*;

public class B {
    public String doIt() {
        return "from B";
    }

    // Note the compiler warning: warning: [exports] class C in module modc may not be visible to all clients that require this module
    public C getMyC() {
        return new C();
    }
}
