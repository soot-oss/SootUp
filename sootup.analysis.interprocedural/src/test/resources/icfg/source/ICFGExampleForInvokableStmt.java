
public class ICFGExampleForInvokableStmt {
    public ICFGExampleForInvokableStmt() {

    }

    public void foo(String s){

    }

    public void bar(String s){

    }

    public void entryPoint() {
        String a = "";
        String b = a + "c";
        foo(a);
        bar(b);
        foo(a);
        foo(b);
    }

}