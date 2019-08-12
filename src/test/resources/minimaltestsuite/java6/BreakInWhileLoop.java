public class BreakInWhileLoop {

    public int breakInWhileLoop(int num) {
        while (true) {
            System.out.println("Current value in While Loop is " + num);
            num--;
            if (num == 0) {
                break;
            }
        }
        return num;
    }
}
