
/** @author Hasitha Rajapakse */

public class TryCatchFinally {

    public void tryCatchFinally() {
        String str = "";
        try {
            str = "try";
            System.out.println(str);
        } catch (Exception e) {
            str = "catch";
            System.out.println(str);
        } finally {
            str = "finally";
            System.out.println(str);
        }
    }

}