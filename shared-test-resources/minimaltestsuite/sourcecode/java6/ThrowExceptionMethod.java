class ThrowExceptionMethod{
    void divideByZero() {
        try{
            int i = 8/0;
        }
        catch(ArithmeticException e){}
    }

    void divideThrowsException() throws ArithmeticException{
        int i=8/0;
    }

    void divideThrowException() {
        throw new ArithmeticException();
    }
}