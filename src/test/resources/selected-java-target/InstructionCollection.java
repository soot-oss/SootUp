import java.util.ArrayList;
import java.util.List;
import java.lang.Exception;
/**
 * Test for source position mapping.
 * 
 * @author Linghui Luo
 *
 */
public class InstructionCollection {
  private long field = 0;

  public void test(int a, int b) {
    int c = a + b;
    call0();
    call1(c);
    call2(a, b);
    String[] d = { "abc", "def" };
    int f = b;
    if (f > 0 && a < 0) {
      List<String> g = new ArrayList<String>(10);
      for (int i = 0; i < g.size(); i++) {
        g.add("No" + i);
      }
      if (g instanceof ArrayList) {
        g.remove(0);
        g.add(d[1]);
      }
    }
  }

  public void call0() {
    field = (long) getClass().getName().length();
  }

  public long call1(int i) {
    field = i;
    return this.field;
  }

  public List<String> call2(int i, int j) {
    return new ArrayList<String>();
  }

}
