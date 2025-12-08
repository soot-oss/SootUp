/** @author Kaustubh Kelkar */

class ReferencingThis{
    int a;int b;

    ReferencingThis(){
        this(10,20);
        System.out.println("this() to invoke current class constructor");
    }

    ReferencingThis(int a,int b){
        this.a= a;
        this.b= b;
        System.out.println("'this' keyword to refer current class instance variables");
    }

    ReferencingThis getObject(){
        System.out.println("'this' keyword to return the current class instance");
        return this;
    }

    void show(){
        System.out.println("'this' keyword as method parameter");
        thisDisplay(this);
    }

    void thisDisplay(ReferencingThis obj){
        System.out.println("'this' keyword to refer current class instance variables");
        System.out.println(a+" "+b);
    }

    void thisMethod(){
        System.out.println(" this keyword as an argument in the constructor call");
        ReferencingThis obj= new ReferencingThis(this.a, this.b);
        obj.show();
    }

    public static void main(String[] args) {
        ReferencingThis referencingThis = new ReferencingThis();
        referencingThis.show();
        referencingThis.thisDisplay(referencingThis);
        referencingThis.thisMethod();
    }
}