public class BreakInWhileLoop {

    public void breakInWhileLoop() {
        int num = 10;
        while (true) {
            num--;
            if (num == 0) {
                break;
            }
        }
    }
}
