package serlam;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
public class DoSerialization {
    public static void main(String[] args) throws Exception {
        float y = 3.14f;
        String s = "foo";
        Test lambda = (Integer x) -> "Hello World " + x + y + s;
        FileOutputStream fos = new FileOutputStream("serlam2.ser");
        ObjectOutputStream out = new ObjectOutputStream(fos);
        out.writeObject(lambda);
        out.close();
    }
}
