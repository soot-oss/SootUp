package qilin.pta.toolkits.zipper;

public class Global {
    private static boolean debug = false;
    private static String flow = null;
    private static boolean enableWrappedFlow = true;
    private static boolean enableUnwrappedFlow = true;
    private static boolean isExpress = false;
    public static final int UNDEFINE = -1;
    private static int tst = UNDEFINE;
    private static int thread = UNDEFINE;
    private static float expressThreshold = 0.05f;

    public static void setDebug(final boolean debug) {
        Global.debug = debug;
    }

    public static boolean isDebug() {
        return Global.debug;
    }

    public static String getFlow() {
        return Global.flow;
    }

    public static void setFlow(String flow) {
        Global.flow = flow;
    }

    public static boolean isEnableWrappedFlow() {
        return Global.enableWrappedFlow;
    }

    public static void setEnableWrappedFlow(boolean enableWrappedFlow) {
        Global.enableWrappedFlow = enableWrappedFlow;
    }

    public static boolean isEnableUnwrappedFlow() {
        return Global.enableUnwrappedFlow;
    }

    public static void setEnableUnwrappedFlow(boolean enableUnwrappedFlow) {
        Global.enableUnwrappedFlow = enableUnwrappedFlow;
    }

    public static boolean isExpress() {
        return Global.isExpress;
    }

    public static void setExpress(final boolean isExpress) {
        Global.isExpress = isExpress;
    }

    public static int getTST() {
        return Global.tst;
    }

    public static void setTST(int tst) {
        Global.tst = tst;
    }

    public static int getThread() {
        return thread;
    }

    public static void setThread(int thread) {
        Global.thread = thread;
    }

    public static float getExpressThreshold() {
        return expressThreshold;
    }

    public static void setExpressThreshold(float expressThreshold) {
        Global.expressThreshold = expressThreshold;
    }

    private static boolean listContext = false;

    public static boolean isListContext() {
        return listContext;
    }

    public static void setListContext(boolean listContext) {
        Global.listContext = listContext;
    }
}
