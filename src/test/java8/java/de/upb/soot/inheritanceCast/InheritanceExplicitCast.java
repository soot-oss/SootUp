package java.de.upb.soot.inheritanceCast;

public class InheritanceExplicitCast {

    public void ExplicitCast(){

        Child c = new Child();
        c.doSth();

        Parent p = (Parent) c;
        p.doSth();
    }


}
