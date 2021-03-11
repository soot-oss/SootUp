package pkgfib;

import pkgfib.internal.*;

public class Fibonacci {
    private static long FIB0 = 0;
    private static long FIB1 = 1;
    private static long FIB2 = 1;

    public static long fib (long n) {
        if (n==0) {
            return FIB0;
        }
        else
        if (n==1) {
            return FIB1;
        }
        else
        if (n==2) {
            return FIB2;
        }
        else {
            return MathHelper.add(fib(n-1) , fib (n-2));
        }
    }
}
