package bachelor.invokedynamic;

public class ResolveMethodHandle2 {
    public static void main(String[] args) {
        Inner i = (a) -> a%7;
        System.out.println(i.bar(1));
    }
    interface Inner {
        int bar(int a);
    }
}