
public class ByteCodeTypeTest {

    A field = new A();
    static int count = 0;
    static final String constant =" new A()";

    /*test string constant*/
    public void constant(){
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


    /*test shift expression - shift expression*/
    public void shiftL() {
        int a = 1;
        long b = 1;
        long c = b << a;
    }

    /*test shift expression - shift expression*/
    public void shiftI() {
        int a = 1;
        long b = 1;
        int c = a << b;
    }

    /*test int long binary expression - logical expression*/
    public void xor1(){
        boolean a = true;
        boolean b = true;
        boolean c =  a^b;
    }

    public void xor2(){
        int a = 1;
        long b = 2;
        long c =  a^b;
    }

    /*test int float binary expression*/
    public void add(){
        int a = 1;
        long b = 1;
        long c = a + b;
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

    public void arrayRef(){
        A[][] arr = new A[3][3];
        A a = arr[1][1];
    }

    public void fieldRef(){
        A newField = this.field;
        count ++ ;
    }

    public void invoke(){
        A a = new A();
        a.method();
    }
}