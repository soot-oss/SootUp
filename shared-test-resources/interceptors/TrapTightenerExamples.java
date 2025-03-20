public class TrapTightenerExamples {

    int example1() {
        int a = 1;
        int c = 0;
        try{
            int b = a;
            c = b/a;
        }catch (ArithmeticException e){
            throw e;
        }
        return c;
    }

}