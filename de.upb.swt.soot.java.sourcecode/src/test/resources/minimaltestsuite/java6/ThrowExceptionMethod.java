class ThrowExceptionMethod{
    void divide() {
        try{
            int i = 8/0;
        }
        catch(ArithmeticException e){}
    }

    void divideByZero() throws ArithmeticException{
        int i=8/0;
    }

    void divideThrows() {
        throw new ArithmeticException();
    }
}