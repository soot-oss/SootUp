import java.util.*;

public class AugEvalFunctionDemos {

    A field = new A();
    static int count = 0;
    static final String constant =" new A()";

    /*test constants*/
    public void constant(){
        int i = 127;
        i = 32111;
        i = -129;
        double d = 1.0;
        String s = "example";
    }

    /*test class constant*/
    public void reflection(){
        Class<?> a = A.class;
    }

    /*test condition expression*/
    public void condition(){
        int a = 1;
        int b = 1;
        boolean c = a < b;
    }

    /*test shift expression*/
    public void shift() {
        int a = 1;
        long b = 1;
        long c = b << a;
        int d = a << b;
    }

    /*test logical expression*/
    public void xor(){
        int a = 1;
        int b = 1;
        int c =  b^a;
        long d = 1L;
        long e =  b^d;
    }

    /*test int float binary expression*/
    public void add(){
        int a = 1;
        float b = 1;
        float c = b + a;
        a++;
    }

    /*test unitary expression - length*/
    public void length(){
        int[] arr = new int[10];
        int b = arr.length;
    }

    /*test instanceof expression*/
    public boolean instanceOf(){
        A a = new A();
        return a instanceof A;
    }

    public void newArrayExpr(){
        A[][] arr = new A[3][3];
        A[] a = arr[1];
    }

    public void invokeExpr(){
        A a = new A();
        a.method();
    }

    public void caughtException1(){
        try{
            int c = 1/0;
        } catch (ArithmeticException e) {
            e.printStackTrace();
        }
    }

    public void caughtException2(){
        try {
            int a = 1;
        }catch(IndexOutOfBoundsException| NumberFormatException e) {
            e.printStackTrace();
        }
    }

    public void fieldRef(){
        A newField = this.field;
        count ++ ;
    }
}