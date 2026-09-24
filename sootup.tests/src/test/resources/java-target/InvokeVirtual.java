
public class InvokeVirtual implements ExampleInterface {
  public String x;

  public boolean equals(InvokeVirtual other) {
    return this.x.equals(other.x);
  }

  @Override
  public void interfaceMethod() {
    System.out.println("abc");
  }

  protected void doStuf() {
    interfaceMethod();
  }

}
