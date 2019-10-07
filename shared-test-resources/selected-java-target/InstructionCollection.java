import java.util.ArrayList;
import java.util.List;
import java.lang.Exception;
import java.lang.reflect.*;
/**
 * Test for source position mapping.
 * 
 * @author Linghui Luo
 */
public class InstructionCollection {
    private long field = 0;
    static int staticField = 42;
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

  public void call01() {
    int irrelefant = 42;
    return;
  }

  private void readSth() {
      long thatAbc = this.field;
      long abc = field;
      int thatElectron = InstructionCollection.staticField;
      int electron = staticField;
  }

  private void callAndAssign(){
      int pi = favouriteNumber();
      int euler = 3;
      euler = favouriteNumber();
  }

    private int favouriteNumber(){
        int res = 0;
        switch(3){
            case 3:
                res = 3;
                break;
            case 6:
                res = 6;
                break;
            default:
        }
        return res;
    }

    void complexOperands(){
        int a;
        int b = 2;
        int c = 3;
        int d = 4;
        int x = a + b + c + d;

        int y =
                3
                *
                        4;
        long cascade = call1( 1 + call1(42) + 33102 );
        long li = 42;
        int i = (int) li;
        Exception sth = new Exception("abc", new Exception("anotherthrowable"));
        boolean zet = !false;
        i++;
    }

    void comparison(){
        int i = 42;
        float f = 3.3f;
        if( i == f){
            int x = 5;
        }
    }

    private void exceptionMethod(){
        try {
            throw new Exception("useful message");
        } catch (Exception e) {

        }
        char [] charbuf = new char [6];
        int len = charbuf.length;
        charbuf[0] = 'F';
        charbuf[1] = 'U';
        charbuf[2] = 'T';
        charbuf[3] = charbuf[1];
        charbuf[4] = 'R';
        charbuf[5] = 'E';
        charbuf[6] = 'S';
        charbuf[7] = 'O';
        charbuf[8] = 'O';
        charbuf[9] = 'T';

    }

    synchronized void atomicone(){
        this.field = 1;
    }

    void atomictwo(){
        synchronized(this) {
            this.field = 2;
        }
        assert( this.field > 0);
    }

}
