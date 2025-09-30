package supercall;

class Demo extends SuperClass {

  public void source(){
    super.method();
  }

  public static void main(String[] args){
    Demo demo = new Demo();
    demo.source();
  }
}

class SuperClass extends SuperSuperClass{

}

class SuperSuperClass {

  void method() { /* doSomething */ }
}