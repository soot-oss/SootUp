import java.io.*;

interface DefaultMethodInterface{

    public void interfaceMethod();
    default void defaultInterfaceMethod();

  /**Add this line after default methods are supported

    {
       System.out.println("Method
    defaultInterfaceMethod() in interface");

    };
   */
}
