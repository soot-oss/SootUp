abstract class ICFGDispatchBase {
    abstract String id(String s);
}

class ICFGDispatchSubA extends ICFGDispatchBase {
    @Override
    String id(String s) {
        return s;
    }
}

class ICFGDispatchSubB extends ICFGDispatchBase {
    @Override
    String id(String s) {
        return "B";
    }
}

interface ICFGDispatchIface {
    void run(String s);
}

class ICFGDispatchIfaceImpl implements ICFGDispatchIface {
    @Override
    public void run(String s) {
        sink(s);
    }

    static void sink(String s) {}
}

public class ICFGDispatchExample {

    public static void main(String[] args) {
        virtualCall();
        interfaceCall();
        tryCatch();
        throwCaughtLocally();
        branch(args.length);
    }

    static void virtualCall() {
        ICFGDispatchBase base = new ICFGDispatchSubA();
        base.id("x");
    }

    static void interfaceCall() {
        ICFGDispatchIface iface = new ICFGDispatchIfaceImpl();
        iface.run("y");
    }

    static String tryCatch() {
        try {
            mayThrow();
        } catch (IllegalStateException e) {
            return "caught";
        }
        return "normal";
    }

    static void mayThrow() {
        throw new IllegalStateException();
    }

    static int throwCaughtLocally() {
        try {
            throw new IllegalStateException();
        } catch (IllegalStateException e) {
            return 1;
        }
    }

    static int branch(int i) {
        int r = 0;
        if (i > 0) {
            r = 1;
        }
        return r;
    }
}
