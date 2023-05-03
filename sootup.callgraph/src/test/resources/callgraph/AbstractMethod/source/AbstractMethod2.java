package am2;

class Main  {
  public static void main(String[] args){
    SuperClass a = new Class();
    a.method();
  }
}

class SuperClass {
  public void method(){
  }
}
abstract class AbstractClass extends SuperClass{
  public abstract void method();
}
class Class extends AbstractClass{
  public void method(){
  }
}