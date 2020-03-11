
/** @author Hasitha Rajapakse */


public class LabelledLoopBreak{
    public void labelledLoopBreak() {
        start:
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (i == 1) {
                    break start;
                }
            }
        }
    }
}