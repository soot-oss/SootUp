public class Runner {
    public static void main(String[] args){
        RootInterface1 if1 = new Class1();
        SubInterface1 if2 = new Class2();
        RootInterface2 if3 = new Class3();

        if1.method1();
        if2.method2();
        if3.method3();
    }
}