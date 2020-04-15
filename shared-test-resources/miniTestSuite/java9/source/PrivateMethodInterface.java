
/** @author Kaustubh Kelkar */
interface PrivateMethodInterface{

    public default void methodInterface(int a, int b) {
        add(a, b);
        sub(a, b);
        System.out.println("methodInterface() in PrivateMethodInterface");
    }
    private void add(int a, int b){
        System.out.println(a+b);
    }

    private static void sub(int a, int b){
        System.out.println(a-b);
    };
}