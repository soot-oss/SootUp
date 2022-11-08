package recur;

class Class {

  public static void method(){ /* do something*/}
  public static void method(int param){ /* do something*/}

  public static void main(String[] args){

    if( args.length == 42){
      Class.method();
    }else{
      main("Recursive Call");
    }
  }
}