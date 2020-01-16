package unsafe;
import sun.misc.Unsafe;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import lib.annotations.callgraph.DirectCall;
public class Demo {
    private TargetInterface objectVar = null;
	@DirectCall(name = "targetMethod", resolvedTargets = "Lunsafe/UnsafeTarget;", returnType = String.class, line = 22)
    public static void main(String[] args) throws Exception {
        Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
        unsafeConstructor.setAccessible(true);
        Unsafe unsafe = unsafeConstructor.newInstance();
        Demo o = new Demo();
        Field objectField = Demo.class.getDeclaredField("objectVar");
        long objectOffset = unsafe.objectFieldOffset(objectField);
        unsafe.compareAndSwapObject(o, objectOffset, null, new UnsafeTarget());
        o.objectVar.targetMethod();
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
