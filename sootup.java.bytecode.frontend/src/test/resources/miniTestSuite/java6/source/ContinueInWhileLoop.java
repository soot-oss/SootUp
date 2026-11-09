
/** @author Hasitha Rajapakse */


public class ContinueInWhileLoop {

    public void continueInWhileLoop(){
        int num = 0;
        while (num < 10) {
            if (num == 5) {
                num++;
                continue;
            }
            num++;
        }
    }
}
