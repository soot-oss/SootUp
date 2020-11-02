package lib2;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    public Type field = new Subtype();
    @DirectCall(name = "method", line = 12, resolvedTargets = {"Llib2/Type;", "Llib2/Subtype;"}, 
    prohibitedTargets = "Llib2/SomeType;")
    public void callOnField(){
        field.method();
    }
}
