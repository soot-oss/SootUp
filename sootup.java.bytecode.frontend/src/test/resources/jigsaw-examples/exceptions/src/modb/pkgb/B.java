package pkgb;

import pkgbinternal.*;

public class B {
    public String doIt() {
        return "from B"; 
    }

    // ----------------------------------------------------------------------

    public String doItThrowException() throws MyException {
        throw new MyException();
    }

    public String doItThrowRuntimeException() {
        throw new MyRuntimeException();
    }

    // ----------------------------------------------------------------------

    public String doItThrowInternalException() throws MyInternalException {
        throw new MyInternalException();
    }

    public String doItThrowInternalRuntimeException() {
        throw new MyInternalRuntimeException();
    }

    // ----------------------------------------------------------------------

    public String doItChainInternalExceptionToRuntimeException() {
        throw new RuntimeException (
                "chained in B.doItChainInternalExceptionToRuntimeException()", 
                new MyInternalException());
    }

    public String doItChainInternalRuntimeExceptionToRuntimeException() {
        throw new RuntimeException (
                "chained in B.doItChainInternalRuntimeExceptionToRuntimeException()", 
                new MyInternalRuntimeException());
    }
}
