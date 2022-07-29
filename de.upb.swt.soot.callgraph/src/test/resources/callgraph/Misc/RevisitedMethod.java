package revisit;

class RevisitedMethod {

  public static void main(String[] args) {
    alreadyVisitedMethod (new C());
    laterTime();
  }

  public static void alreadyVisitedMethod (A a){
    a.newTarget();
  }

  public static void laterTime (){
    alreadyVisitedMethod (new B());
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

