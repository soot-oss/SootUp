public class CastCounterDemos {

    public void invokeStmt(){
       Super1 s1  = new Sub1();
       int i  = 1;
       Sub2 s2 = new Sub2();
       s1.m(i, s2);
    }

    public void assignStmt(){
        Super1[] arr = new Super1[10];
        arr[0] = new Sub1();
        Super1 var = arr[2];
    }
}
