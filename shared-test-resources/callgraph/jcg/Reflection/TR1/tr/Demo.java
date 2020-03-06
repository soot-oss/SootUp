package tr;
import lib.annotations.callgraph.IndirectCall;
class Demo {
    static String target() { return "42"; }
    @IndirectCall(
        name = "target", returnType = String.class, line = 13,
        resolvedTargets = "Ltr/Demo;"
    )
    public static void main(String[] args) throws Exception {
        Demo.class.getDeclaredMethod("target").invoke(null);
    }
}
