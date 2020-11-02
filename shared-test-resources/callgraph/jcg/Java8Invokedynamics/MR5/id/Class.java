package id;
import java.util.function.Supplier;
import lib.annotations.callgraph.IndirectCall;
class Class {
    public static double sum(double a, double b) { return a + b; }
    @FunctionalInterface public interface FIDoubleDouble {
        double apply(double a, double b);
    }
    @IndirectCall(
       name = "sum", returnType = double.class, parameterTypes = {double.class, double.class}, line = 19,
       resolvedTargets = "Lid/Class;")
    public static void main(String[] args){
        FIDoubleDouble fidd = Class::sum;
        fidd.apply(1d,2d);
    }
}
