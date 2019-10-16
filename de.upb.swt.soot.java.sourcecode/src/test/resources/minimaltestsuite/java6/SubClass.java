class SubClass extends SuperClass {
    public int aa = 1;
    protected int bb = 2;
    int cc = 3;
    private int dd = 4;

    public void subclassMethod() { 
        aa=10;
        bb=20;
        cc=30;
        dd=40;
    }

    public void superclassMethod(){
        super.superclassMethod();
        a=100;
        b=200;
        c=300;
<<<<<<< HEAD:de.upb.swt.soot.java.sourcecode/src/test/resources/minimaltestsuite/java6/SubClass.java
=======
        

>>>>>>> 9c1aaee87458482e6e9b89d556d7b257280f004c:de.upb.swt.soot.java.sourcecode/src/test/resources/minimaltestsuite/java6/SubClass.java
    }
}