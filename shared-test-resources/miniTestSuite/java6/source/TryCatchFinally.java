
/** @author Hasitha Rajapakse */

public class TryCatchFinally {
    public void tryCatch() {
        String str = "";
        try {
            str = "try";
            System.out.println(str);
        } catch (Exception e) {
            str = "catch";
            System.out.println(str);
        }
    }

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

    public void tryCatchCombined() {
        String str = "";
        try {
            str = "try";
            System.out.println(str);
        } catch (RuntimeException | StackOverflowError e) {
            str = "catch";
            System.out.println(str);
        }
    }

    public void tryCatchFinallyCombined() {
        String str = "";
        try {
            str = "try";
            System.out.println(str);
        } catch (RuntimeException | StackOverflowError e) {
            str = "catch";
            System.out.println(str);
        } finally {
            str = "finally";
            System.out.println(str);
        }
    }

    public void tryCatchNested() {
        String str = "";
        try {
            str = "1try";
            System.out.println(str);
            try {
                str = "2try";
                System.out.println(str);
            } catch (Exception e) {
                str = "2catch";
                System.out.println(str);
            }
        } catch (Exception e) {
            str = "1catch";
            System.out.println(str);
        }
    }

    public void tryCatchFinallyNested() {
        String str = "";
        try {
            str = "1try";
            System.out.println(str);
            try {
                str = "2try";
                System.out.println(str);
            } catch (Exception e) {
                str = "2catch";
                System.out.println(str);
            }
        } catch (Exception e) {
            str = "1catch";
            System.out.println(str);
        }finally {
            str = "1finally";
            System.out.println(str);
        }
    }

    public void tryCatchNestedInCatch() {
        String str = "";
        try {
            str = "1try";
            System.out.println(str);
        } catch (Exception e) {
            str = "1catch";
            System.out.println(str);
            try {
                str = "2try";
                System.out.println(str);
            } catch (Exception ex) {
                str = "2catch";
                System.out.println(str);
            }
        }
    }

    public void tryCatchFinallyNestedInCatch() {
        String str = "";
        try {
            str = "1try";
            System.out.println(str);
        } catch (Exception e) {
            str = "1catch";
            System.out.println(str);
            try {
                str = "2try";
                System.out.println(str);
            } catch (Exception ex) {
                str = "2catch";
                System.out.println(str);
            }
        }finally {
            str = "1finally";
            System.out.println(str);
        }
    }


    public void tryCatchFinallyNestedInFinally() {
        String str = "";
        try {
            str = "1try";
            System.out.println(str);
        } catch (Exception e) {
            str = "1catch";
            System.out.println(str);
        }finally {
            str = "1finally";
            System.out.println(str);
            try {
                str = "2try";
                System.out.println(str);
            } catch (Exception e) {
                str = "2catch";
                System.out.println(str);
            }
        }
    }

}