public class TrapTightenerExamples {

    void example1() {
        int a = 1;
        int c = 0;
        try{
            int b = a;
            c = b/a;
        }catch (ArithmeticException e){
            throw e;
        }
    }

    void example2() {
        int a = 1;
        int b = 0;
        try{
            int c = 2;
            int d = b/a;
            int e = 0;
            e = a/b;
        }catch (ArithmeticException e){
            throw e;
        }
    }

    void example3() {
        int a = 1;
        int b = 0;
        try{
            int c = b/a;
            a = b;
        }catch (ArithmeticException e){
            throw e;
        }
    }

    void example4() {
        int a = 1;
        int b = 0;
        try{
            a = b;
        }catch (ArithmeticException e){
            throw e;
        }
    }

    void example5() {
        int a = 1;
        int b = 0;
        int[] arr = {1, 2, 3};
        try{
            int c = 2;
            int d = c/b;
            arr[3] = d;
            int e = 5;
        }catch (ArithmeticException e1){
            throw e1;
        }catch (NullPointerException e2){
            throw e2;
        }
    }
}