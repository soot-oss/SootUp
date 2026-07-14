package pkgmain;

import myservice.*;

public class Main {
    public static void main(String[] args) {
        IService service = Factory.create();
        if (service != null) {
           System.out.println("We finally use this implementation: " + service.getName());
        }
    }
}