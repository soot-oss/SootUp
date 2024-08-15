public class InfiniteLoops {
    void tc1() {
        int x = 10;
        while (true) {
            if (x == 5) {
                break;
            }
        }
        System.out.println(x);
    }

    void tc2() {
        int temp = 0;
        do {
            if (temp == 10) {
                break;
            }
        } while (true);
    }

    void tc3() {
        int mul = 1;
        for (; ; ) {
            if (mul > 1000) break;
        }
    }
}
