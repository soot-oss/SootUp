public class File2 {

    public int someMethod() {
        int a = 3 * 10 + 7;
        int b = a + 5;
        int c = (a * b) - (a * b);
        int d;
        int[] e = new int[5];
        int cond = (int)(Math.random() * 2);
        switch (cond) {
            case 0:
                d = c + 100;
                e[2] = 30;
            case 1:
                d = b + 100;
                e[1] = e[2];
            default:
                d = -100;
        }
        System.out.println(d);
        return a;
    }
}
