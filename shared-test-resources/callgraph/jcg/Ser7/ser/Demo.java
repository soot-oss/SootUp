package ser;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import lib.annotations.callgraph.DirectCall;
public class Demo implements Serializable {
    static final long serialVersionUID = 42L;
    public Object replace() { return this; }
    @DirectCall(name = "replace", returnType = Object.class, resolvedTargets = "Lser/Demo;", line = 17)
    private Object readResolve() throws ObjectStreamException {
        return replace();
    }
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Demo obj = (Demo) in.readObject();
        in.close();
    }
}
