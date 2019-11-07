import java.io.*;

class DefaultMethodInterfaceImpl implements DefaultMethodInterface {

    public void interfaceMethod(){
        System.out.println("Method interfaceMethod() is implemented");
    }

    public void defaultInterfaceMethod(){

        DefaultMethodInterface.super.defaultInterfaceMethod();
        System.out.println("Method defaultInterfaceMethod() is implemented");
    };

    public static void main(String[] args) {
    DefaultMethodInterfaceImpl obj= new DefaultMethodInterfaceImpl();
    obj.interfaceMethod();
    obj.defaultInterfaceMethod();
  }
}