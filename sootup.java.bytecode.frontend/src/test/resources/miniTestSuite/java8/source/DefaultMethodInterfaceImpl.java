import java.io.*;

class DefaultMethodInterfaceImpl implements DefaultMethodInterface {

    public void interfaceMethod(){
        System.out.println("Method interfaceMethod() is implemented");
    }

    public void defaultInterfaceMethod(){

        /**Add this line after default methods are supported

         DefaultMethodInterface.super.defaultInterfaceMethod();

         * */
        System.out.println("Method defaultInterfaceMethod() is implemented");
    };
}