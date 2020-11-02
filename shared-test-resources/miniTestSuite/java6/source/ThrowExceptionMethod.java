/** @author Kaustubh Kelkar */
class ThrowExceptionMethod{

	void divideByZero() throws ArithmeticException{
			int i=8/0;
	}

	void throwCustomException() throws CustomException {
		throw new CustomException("Custom Exception");
	}
	
	public static void main (String args[]){
		try{
			ThrowExceptionMethod obj = new ThrowExceptionMethod();
			obj.throwCustomException();
		} catch( CustomException e){
			System.out.println(e.getMessage());
		}
	}
}

class CustomException extends Exception{
    public CustomException(String message){
        super(message);
    }
}