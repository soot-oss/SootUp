package tr;
import lib.annotations.callgraph.IndirectCall;
class Demo {
    public String target() { return "Demo"; }
    @IndirectCall(
        name = "target", returnType = String.class, line = 13,
        resolvedTargets = "Ltr/Demo;"
    )
    void caller() throws Exception {
        Demo.class.getDeclaredMethod("target").invoke(this);
    }
    public static void main(String[] args) throws Exception {
        new Demo().caller();
    }
}
