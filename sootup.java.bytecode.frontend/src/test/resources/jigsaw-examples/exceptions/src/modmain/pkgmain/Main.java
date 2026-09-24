package pkgmain;

import pkgb.B;
import pkgb.MyException;
import pkgb.MyRuntimeException;

public class Main {
    public static void main(String[] args) {
        Main mymain = new Main();		
        B myb = new B();

// normal call
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt());
        System.out.println();

// try-catch for visible MyException
        try {
            myb.doItThrowException();
        } 
        catch (MyException ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }

// try-catch for visible MyRuntimeException
        try {
            myb.doItThrowRuntimeException();
        } 
        catch (MyRuntimeException ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }

        // ----------------------------------------------------------------------------------------------------------------------

// try-catch for invisible MyInternalException
        try {
            myb.doItThrowInternalException();
        } 
        // does not compile
        // catch (MyInternalException ex) {
        catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }

        // ----------------------------------------------------------------------------------------------------------------------

// try-catch for invisible MyInternalRuntimeException
        try {
            myb.doItThrowInternalRuntimeException();
        } 
        // does not compile
        // catch (MyInternalRuntimeException ex) {
        catch (RuntimeException ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }

        // ----------------------------------------------------------------------------------------------------------------------

// try-catch for chained invisible MyInternalException
        try {
            myb.doItChainInternalExceptionToRuntimeException();
        } 
        catch (Exception ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }

// try-catch for chained invisible MyInternalRuntimeException
        try {
            myb.doItChainInternalRuntimeExceptionToRuntimeException();
        } 
        catch (RuntimeException ex) {
            ex.printStackTrace(System.out);
            System.out.println();
        }
    }
}
