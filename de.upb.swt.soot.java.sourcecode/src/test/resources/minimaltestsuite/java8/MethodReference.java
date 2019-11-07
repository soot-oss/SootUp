@FunctionalInterface
interface MyInterface{
    void display();
}
public class MethodReference {
    public void methodRefMethod(){
        System.out.println("Instance Method");
        MethodReference obj1 = new MethodReference();
        // Method reference using the object of the class
        MyInterface ref1 = obj1::methodRefMethod;
        // Calling the method of functional interface
        ref1.display();
    }
    /*
    public static void main(String[] args) {
        MethodReference obj = new MethodReference();
        obj.methodRefMethod();
        System.exit(0);

    }*/
}