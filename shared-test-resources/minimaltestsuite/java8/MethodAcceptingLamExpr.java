import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

interface Percentage {
    public double calcPercentage( double value);
}

public class MethodAcceptingLamExpr {

    public void lambdaAsParamMethod(){
//        Percentage percentageValue = (value -> value/100);
//        System.out.println("Percentage : " + percentageValue.calcPercentage(45.0));
    }
}

