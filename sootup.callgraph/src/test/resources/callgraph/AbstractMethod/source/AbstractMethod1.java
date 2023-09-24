package am1;

class Main  {
  public static void main(String[] args){
    AbstractClass a = new Class();
    a.method();
  }
}

abstract class AbstractClass {
  public abstract void method();
}
class Class extends AbstractClass{
  public void method(){
  }
}