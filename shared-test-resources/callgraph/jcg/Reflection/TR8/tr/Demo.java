package tr;
import java.lang.reflect.Field;
import lib.annotations.callgraph.IndirectCall;
public class Demo {
    public Target field;
    @IndirectCall(
        name = "target", line = 18, resolvedTargets = "Ltr/CallTarget;"
    )
    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();
        demo.field = new CallTarget();
        Field field = Demo.class.getField("field");
        Target t = (Target) field.get(demo);
        t.target();
    }
}
interface Target {
    void target();
}
class CallTarget implements Target {
    public void target(){ /* do something */ }
}
class NeverInstantiated implements Target {
    public void target(){ /* do something */ }
}
