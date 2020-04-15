import java.io.*;
/** @author Kaustubh Kelkar */
class MultiInterfaceImplClass implements InterfaceImpl, InterfaceImplDummy{

    public void interfaceMethod(){
        System.out.print("Method from InterfaceImpl is implemented");
        System.out.println("Variable from InterfaceImpl is "+a);
    };

}