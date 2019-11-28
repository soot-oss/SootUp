/** @author Kaustubh Kelkar */
class ThrowExceptionMethod{

    void divideByZero() throws ArithmeticException{
        int i=8/0;
    }
    void throwCustomException() {
        throw new CustomException();
    }

}

class CustomException extends Exception{
    CustomException(){
        System.out.println("CustomException is here");
    }
}