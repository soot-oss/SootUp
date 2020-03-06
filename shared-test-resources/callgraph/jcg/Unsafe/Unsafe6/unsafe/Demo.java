package unsafe;
import sun.misc.Unsafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    private Object objectVar = null;
    @DirectCall(name = "targetMethod", resolvedTargets = "Lunsafe/SafeTarget;", returnType = String.class, line = 24)
    public static void main(String[] args) throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();
        Demo demo = new Demo();
        Field objectField = Demo.class.getDeclaredField("objectVar");
        long objectOffset = unsafe.objectFieldOffset(objectField);
        demo.objectVar = new SafeTarget();
        TargetInterface target = (TargetInterface) unsafe.getObjectVolatile(demo, objectOffset);
        target.targetMethod();
    }
}
interface TargetInterface {
    String targetMethod();
}
class UnsafeTarget implements TargetInterface{
	public String targetMethod() {
		return "UnsafeTarget";
	}
}
class SafeTarget implements TargetInterface {
    public String targetMethod() {
        return "SafeTarget";
    }
}
