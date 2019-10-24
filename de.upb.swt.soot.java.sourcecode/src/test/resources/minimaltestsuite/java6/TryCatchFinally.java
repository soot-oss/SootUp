
/** @author: Hasitha Rajapakse */

public class TryCatchFinally {
    public void tryCatchFinally() {
        String str = "";
        try {
            str = "this is try block";
            int i = 0;
            i++;
            System.out.println(i);
        } catch (Exception e) {
            str = "this is catch block";
        } finally {
            str = "this is finally block";
        }
    }
}