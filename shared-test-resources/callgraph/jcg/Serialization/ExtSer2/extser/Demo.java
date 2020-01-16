package extser;
import java.io.Externalizable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectOutput;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.ObjectInput;
import java.io.IOException;
import lib.annotations.callgraph.DirectCall;
public class Demo implements Externalizable {
    @DirectCall(name = "callback", resolvedTargets = "Lextser/Demo;", line = 17)
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        callback();
    }
    public void writeExternal(ObjectOutput out) throws IOException {
        callback();
    }
    public void callback() { }
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Demo obj = (Demo) in.readObject();
        in.close();
    }
}
