package revisitrecur;

class RecursiveRevisitedMethod {

  public static void main(String[] args) {
    recursiveAlreadyVisitedMethod (new B());
  }
  public static void recursiveAlreadyVisitedMethod (A b){
    b.newTarget();
    recursiveAlreadyVisitedMethod(createC());
  }
  public static C createC(){
    return new C();
  }
}

public class A{
  public int newTarget(){
    return 8;
  }
}

public class B extends A {
  public int newTarget(){
    return 7;
  }
}

public class C extends A{
  public int newTarget(){
    return 1;
  }
}


