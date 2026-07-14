
/** @author Hasitha Rajapakse */


public class BooleanOperators{

    public void relationalOpEqual(){
        int i =  0;
        while (i<=10){
            i++;
            if (i==5){
                break;
            }
        }
    }

    public void relationalOpNotEqual(){
        int i =  0;
        String str = "";
        while (i<10){
            i++;
            if (i!=5){
                str = "i != 5";
            }
        }
    }

    public void complementOp(){
        boolean b = true;
        if(b){
            b = !b;
        }
    }

    public void logicalOpAnd(){
        boolean a = true;
        boolean b = true;
        boolean c = false;
        boolean d = false;
        String str = "";

        if(a & b){
            str = "A";
        }

        if (c & d){
            str = "B";
        }

        if (a & c){
            str = "C";
        }

        if (d & b){
            str = "D";
        }
    }

    public void logicalOpOr(){
        boolean a = true;
        boolean b = true;
        boolean c = false;
        boolean d = false;
        String str = "";

        if(a | b){
            str = "A";
        }

        if (c | d){
            str = "B";
        }

        if (a | c){
            str = "C";
        }

        if (d | b){
            str = "D";
        }
    }

    public void logicalOpXor(){
        boolean a = true;
        boolean b = true;
        boolean c = false;
        boolean d = false;
        String str = "";

        if(a ^ b){
            str = "A";
        }

        if (c ^ d){
            str = "B";
        }

        if (a ^ c){
            str = "C";
        }

        if (d ^ b){
            str = "D";
        }
    }

    public void ConditionalOpAnd(){
        boolean a = true;
        boolean b = true;
        boolean c = false;
        boolean d = false;
        String str = "";

        if(a && b){
            str = "A";
        }

        if (c && d){
            str = "B";
        }

        if (a && c){
            str = "C";
        }

        if (d && b){
            str = "D";
        }
    }

    public void conditionalOpOr(){
        boolean a = true;
        boolean b = true;
        boolean c = false;
        boolean d = false;
        String str = "";

        if(a || b){
            str = "A";
        }

        if (c || d){
            str = "B";
        }

        if (a || c){
            str = "C";
        }

        if (d || b){
            str = "D";
        }
    }

    public void conditionalOp(){
        int i = 5;
        String str = "";
        str = i <10 ? "i less than 10" : "i greater than 10";
    }
}