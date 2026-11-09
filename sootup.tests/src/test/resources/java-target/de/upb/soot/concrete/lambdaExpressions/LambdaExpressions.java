package de.upb.sootup.concrete.lambdaExpressions;

public class LambdaExpressions {

  private static void doSomethingStatic() {
    System.out.println("something");
  }

  public void lambdaRet() {
    functionalRet f1 = () -> "foo";
    System.out.println(f1.eval());
  }

  public void lambdaParam() {
    functionalArg f1 = (arg) -> System.out.println(arg);
    f1.eval("arg");
  }

  public void lambdaVoid() {
    functionalVoid f1 = () -> System.out.println("void");
    f1.eval();
  }

  public void passToMethod() {
    doEval(() -> System.out.println("passed as param"));
  }

  private void doEval(functionalVoid re) {
    re.eval();
  }

  public void functionPointer() {
    LambdaExpressions es = new LambdaExpressions();
    doEval(es::doSomething);
  }

  private void doSomething() {
    System.out.println("something");
  }

  public void functionPointerStatic() {
    LambdaExpressions es = new LambdaExpressions();
    doEval(LambdaExpressions::doSomethingStatic);
  }

}
