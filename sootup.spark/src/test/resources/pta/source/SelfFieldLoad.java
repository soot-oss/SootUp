public class SelfFieldLoad {

    public static void main(String[] args) {
        Node a = new Node(); // @1
        Node b = new Node(); // @2
        Node c = new Node(); // @3
        a.f = c;
        b.f = c;
        Node x;
        if (args.length > 0) {
            x = a;
        } else {
            x = b;
        }
        while (x != null) {
            x = x.f; // self-referential load across the loop back-edge: base and target are the same local
        }
    }

    static class Node {
        Node f;
    }

}
