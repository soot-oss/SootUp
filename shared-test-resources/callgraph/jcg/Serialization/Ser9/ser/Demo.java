package ser;
import java.io.Serializable;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.IOException;
public class Demo extends Superclass implements Serializable {
    static final long serialVersionUID = 42L;
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
    }
    public static void main(String[] args) throws Exception {
        FileInputStream fis = new FileInputStream("test.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Demo obj = (Demo) in.readObject();
        in.close();
    }
}
