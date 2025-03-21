public class Main{
  public static void main(String[] args) {
    AbstractClass a = new SubClassMethodImplemented();
    AbstractClass b = new SubClassMethodNotImplemented();
    a.method();

    Interface c = new InterfaceNoImplementation();
    Interface d = new InterfaceImplementation();
    d.defaultMethod();
  }
}