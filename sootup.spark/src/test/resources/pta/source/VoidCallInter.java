public class VoidCallInter {

    public static void main(String[] args) {
        O p = new O();
        O q = p;
        O r = new O();
        p.f = r;
        O t = new O();
        copyValue(t, q);   // JInvokeStmt: void call — tests caseInvokeStmt fix
    }

    static void copyValue(O dst, O src) {
        dst.f = src.f;
    }

    static class O {
        O f;
    }

}
