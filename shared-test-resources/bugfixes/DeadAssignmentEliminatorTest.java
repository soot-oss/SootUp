public class DeadAssignmentEliminatorTest {
    void tc1() {
        int x = 10;
        x = 30;
        int temp = x;
        if (temp > 5) {
            x = 40;
            System.out.println(x);
            temp = temp;
        }
        System.out.println(x);
        x = 20;
        temp = 30;
        System.out.println(temp);
    }

    void tc2() {
        String x = "abc";
        x = "cde";
        if (x.length() > 2) {
            x = "3";
            x = "if";
        }
        System.out.println(x);
    }
}