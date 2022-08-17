import java.io.Serializable;

public class Class4 extends Class2 implements Serializable, Cloneable{

    @Override
    public void method1(){
        System.out.println("I'm method 1 in Class4");
    }

    @Override
    public void method2(){
        System.out.println("I'm method2 in Class4");
    }

    @Override
    public void method3(){
        System.out.println("I'm method3 in Class4");
    }

}