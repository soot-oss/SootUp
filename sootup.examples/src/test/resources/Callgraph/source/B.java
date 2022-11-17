public class B {
  public void calc(A a) {
    a.calc(1); // can be A.calc, B.calc, C.calc
  }
}