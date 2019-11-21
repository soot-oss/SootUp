@FunctionalInterface
interface MyInterface{
    void display();
}
public class MethodReference {
    public void methodRefMethod(){
        System.out.println("Instance Method");
        MethodReference obj1 = new MethodReference();
    /**
     * Uncomment when WALA supports lambda expression MyInterface ref1 = obj1::methodRefMethod;
     * ref1.display();
     */
  }
}