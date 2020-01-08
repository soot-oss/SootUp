package ser;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.ObjectInputValidation;
import java.io.InvalidObjectException;
import lib.annotations.callgraph.DirectCall;
public class Demo implements Serializable, ObjectInputValidation {
    static final long serialVersionUID = 42L;
    public void callback() { }
    @DirectCall(name = "callback", resolvedTargets = "Lser/Demo;", line = 19)
    public void validateObject() throws InvalidObjectException {
        callback();
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.registerValidation(this, 0);
        in.defaultReadObject();
    }
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Demo obj = (Demo) in.readObject();
        in.close();
    }
}
