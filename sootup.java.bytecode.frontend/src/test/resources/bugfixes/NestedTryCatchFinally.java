import java.io.FileInputStream;
import java.io.File;
import java.io.ObjectInputStream;

public class NestedTryCatchFinally {

    private static String test0(File storedResults) throws Exception {
        try {
            FileInputStream file = new FileInputStream(storedResults);
            try {
                ObjectInputStream stream = new ObjectInputStream(file);
                try {
                    return (String) stream.readObject();
                } finally {
                    stream.close();
                }
            } finally {
                file.close();
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

}