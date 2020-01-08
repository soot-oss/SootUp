package tr;
import lib.annotations.callgraph.IndirectCall;
class Demo {
    public static String target(String parameter) { return "Value: " + parameter; }
    @IndirectCall(
        name = "target", returnType = String.class, parameterTypes = String.class, line = 13,
        resolvedTargets = "Ltr/Demo;"
    )
    public static void main(String[] args) throws Exception {
        Demo.class.getDeclaredMethod("target", String.class).invoke(null, "42");
    }
}
