public class ArrayReceiver {

    public static void main(String[] args) {
        int[] a = new int[1];
        boolean same = a.equals(a); // invokevirtual <java.lang.Object: equals>, the receiver is an array
    }

}
