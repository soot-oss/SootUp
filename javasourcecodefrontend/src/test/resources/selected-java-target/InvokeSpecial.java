import java.util.ArrayList;

public class InvokeSpecial extends Object {

  private int privateMethod() {
    return 5;
  }

  public void specialInvokeInstanceInit() {
    ArrayList<String> list = new ArrayList<String>();
    list.add("item1");
  }

  public void specialInvokePrivateMethod() {
    privateMethod();
  }

  public String specialInvokeSupperClassMethod() {
    String s = super.toString();
    return s;
  }

}
