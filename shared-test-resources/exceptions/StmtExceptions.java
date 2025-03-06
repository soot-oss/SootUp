import java.util.HashMap;
import java.util.Map;

class StmtExceptions{

    public void testInvokeStmts(){
        System.currentTimeMillis();
        HashMap<String, String> hm = new HashMap<>();
        hm.put("now", "bar");
        Map<String, String> m = hm;
        m.put("foo", "baz");
    }

    public void testAssignStmts(){
        Number [][] multiArr = new Integer[5][-2]; // test rightOp is NewMultiArrayExpr
        Number [] arr = new Integer[5]; // test rightOp is NewArrayExpr
        for(int i=0; i<arr.length; i++){
            arr[i] = i;  // test leftOp is ArrayRef
        }
        SampleClass sc = new SampleClass(5); // test righOp is StaticInvokeExpr and rightOp is NewExpr
        Integer a = (Integer) arr[4];  // test rightOp is CastExpr(right cast)
        Number b = sc.getRf(); // test righOp is VirtualInvokeExpr
        boolean c = sc instanceof SampleClass; // test InstanceOfExpr
        sc.rf = SampleClass.sf;
        sc.rf = arr.length;
        int d = 0;
        a  = 10 / d;
    }

    public void testMonitorStmts(){
        SampleClass sc = new SampleClass(5);
        synchronized (sc){
            sc.rf = 100;
        }
    }
}