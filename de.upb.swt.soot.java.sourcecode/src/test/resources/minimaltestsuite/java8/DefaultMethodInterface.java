import java.io.*;

interface DefaultMethodInterface{

    public void interfaceMethod();
    default void defaultInterfaceMethod(){
        System.out.println("Method defaultInterfaceMethod() in interface");
    };
}