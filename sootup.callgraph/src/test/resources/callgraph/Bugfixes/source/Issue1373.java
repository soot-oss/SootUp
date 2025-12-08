package issue1373;

class B extends A {
    static A a;
    static {
        m();
        a = new A();
    }
    public  static void m(){}
    public  static void main(String[] args){
        m();
    }
}

class A {
    static int i;
    static {
        i=10;
    }
}