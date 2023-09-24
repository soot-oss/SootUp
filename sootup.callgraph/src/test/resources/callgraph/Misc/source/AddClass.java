package update.operation.cg;

class Class {

  public static void method(){ /* do something*/}
  public static void method(int param){ /* do something*/}

  public static void main(String[] args){
    Class.method();
  }
}

class AdderA {
  public static void method(){ Class.method();  }
  public static void method(int param){
    Class.method();
  }
  public static void method1(){  /* do something*/ }

  public static void main(String[] args){
    Class.main("Something");
  }
}
