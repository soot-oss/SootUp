
/** @author Hasitha Rajapakse */


public class AbstractClass extends A{

    void a(){
        System.out.println("abstract class");
    }

    public void abstractClass(){
        A obj = new AbstractClass();
        obj.a();
    }

}