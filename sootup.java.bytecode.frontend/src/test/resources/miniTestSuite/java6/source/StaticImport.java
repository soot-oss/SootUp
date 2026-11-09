import static java.lang.System.*;
import static java.lang.Math.*;

class StaticImport{

    public void mathFunctions(){
        out.println(sqrt(4));
        out.println(pow(2,5));
        out.println(ceil(5.6));
        out.println("Static import for System.out");
    }
    public static void main(String[] args){
        StaticImport staticImport = new StaticImport();
        staticImport.mathFunctions();
    }
}