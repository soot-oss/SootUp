import java.lang.Exception.ArithmaticException;

class ThrowExceptionMethod{
    void divide() {
        try{
            int i = 8/0;
        }
        catch(ArithmeticException e){}
    }

    void divideByZero() throws ArithmaticException{
        int i=8/0;
    }
}