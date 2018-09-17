package java.de.upb.soot.inheritanceCast;

public class InheritanceImplicitCast {

    public void ImplicitCast(){

        Child c = new Child();
        c.doSth();

        Parent p = c;
        p.doSth();
    }


}
