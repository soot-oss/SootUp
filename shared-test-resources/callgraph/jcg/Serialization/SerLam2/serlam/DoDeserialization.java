package serlam;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import lib.annotations.callgraph.IndirectCall;
public class DoDeserialization {
    @IndirectCall(
            name = "readResolve",
            line = 18,
            resolvedTargets = "Ljava/lang/invoke/SerializedLambda;")
    public static void main(String[] args) throws Exception {
        DoSerialization.main(args);
        FileInputStream fis = new FileInputStream("serlam2.ser");
        ObjectInputStream in = new ObjectInputStream(fis);
        Object obj = in.readObject();
        in.close();
    }
}
