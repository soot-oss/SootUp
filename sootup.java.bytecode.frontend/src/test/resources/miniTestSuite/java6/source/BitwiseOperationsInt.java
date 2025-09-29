
/** @author Hasitha Rajapakse */

public class BitwiseOperationsInt {

    public void bitwiseOpAnd(){
        int a = 70;
        int b = 20;
        int c = a&b;
    }

    public void bitwiseOpOr(){
        int a = 70;
        int b = 20;
        int c = a|b;
    }

    public void bitwiseOpXor(){
        int a = 70;
        int b = 20;
        int c = a^b;
    }

    public void bitwiseOpComplement(){
        int a = 70;
        int b = ~a;
    }

    public void bitwiseOpSignedRightShift(){
        int a = 70;
        int b = a >> 5;
    }

    public void bitwiseOpLeftShift(){
        int a = 70;
        int b = a << 5;
    }

    public void bitwiseOpUnsignedRightShift(){
        int a = 70;
        int b = a >>> 5;
    }
}