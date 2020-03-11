
/** @author Hasitha Rajapakse */


public class BreakInWhileLoop {

    public void breakInWhileLoop() {
        int num = 10;
        int i = 5;
        while (num > 0) {
            num--;
            if (num == i) {
                break;
            }
        }
    }
}
