public class ControlStatements {

    int a = 10;
    int b = 20;
    int c = 30;

    public void greater(int a, int b, int c) {

        // simple if else
        if(a < b) {
            System.out.println("IF: " + a + " is smaller than " + b);
        }
        else {
            System.out.println("ELSE: " + a + " is greater than " + b);
        }

        // Simple switch case
        switch (a) {
            case 10:
                System.out.println("SWITCH CASE: " + a + " is smaller than " + b);
            default:
                System.out.println("SWITCH DEFAULT: " + a + " is smaller than " + b);
        }

        while(a < b) {
            System.out.println("a is smaller than b");

            // Nested: switch in while
            switch (a) {
                case 10:
                    System.out.println("SWITCH CASE: " + a + " is smaller than " + b);
                default:
                    System.out.println("SWITCH DEFAULT: " + a + " is smaller than " + b);
            }

            //Nested: if in while
            if(b == a) {
                System.out.println("b = a");
            }
            else {
                System.out.println(" a and b are not equal");
            }
            a++;
        }

        // Simple for
        for(int i = 0; i<a; i++) {
            b++;
        }

        // Nested: if in for
        for(int i = 0; i < a; i++) {
            if(b > a || c > b) {
                a++;
            }
            else if(a != b) {
                System.out.println("Not equal");
            }
            else if(a == b) {
                System.out.println("Equal");
            }
            else if(b > a && c > b) {
                c--;
            }
        }

        // Nested: for in for
        for(int i = 0; i < a; i++) {
            // Nested: while in for
            for(int j = 0; j <= i; j++){
                // Nested: if in while
                while(j < 4) {
                    if (j == 2) {
                        System.out.println("J is 2");
                        break;
                    }
                    else {
                        System.out.println("J is not 2");
                        break;
                    }
                }
            }
        }
    }

    public static void main(String [] args) {
        ControlStatements c1 = new ControlStatements();
        c1.greater(10, 20, 30);
        c1.greater(20, 10, 30);
        System.out.println("End of execution");
    }
}