/** @author Kaustubh Kelkar */
class ThrowExceptionMethod{

    void divideByZero() throws ArithmeticException{
        int i=8/0;
    }
    void throwCustomException(){
		try{
        throw new CustomException("Custom Exception");}
		catch( CustomException e){
			System.out.println(e.getMessage());
		}
    }
}

class CustomException extends Exception{
    public CustomException(String message){
        super(message);
    }
}