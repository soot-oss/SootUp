package recur;

class Class {

  public static void method(){ /* do something*/}
  public static void method(int param){ /* do something*/}

  public static void main(String[] args){
    if( args != null){
      Class.method();
    }else{
      main(null);
    }
  }
}