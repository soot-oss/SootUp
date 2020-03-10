package ser;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import lib.annotations.callgraph.DirectCall;
public class Demo implements Serializable {
    static final long serialVersionUID = 42L;
    public Object replace() { return this; }
	@DirectCall(name = "replace", returnType = Object.class, resolvedTargets = "Lser/Demo;", line = 17)
    private Object writeReplace() throws ObjectStreamException {
    	return replace();
    }
    public static void main(String[] args) throws Exception {
    	Demo serialize = new Demo();
    	FileOutputStream fos = new FileOutputStream("test.ser");
    	ObjectOutputStream out = new ObjectOutputStream(fos);
    	out.writeObject(serialize);
    	out.close();
    }
}
