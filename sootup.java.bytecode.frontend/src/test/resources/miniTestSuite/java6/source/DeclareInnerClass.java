class DeclareInnerClass{
    public int a=1;
    public DeclareInnerClass getDeclareInnerClass(){
        return DeclareInnerClass.this;
    }
    public void methodDisplayOuter(){
        System.out.println("methodDisplayOuter");
    }

    public class InnerClass{
        public void methodDisplayInner(){
            System.out.println("methodDisplayInner");
        }

    }

  public static void main(String[] args) {
        DeclareInnerClass declareInnerClass = new DeclareInnerClass();
        declareInnerClass.methodDisplayOuter();
        DeclareInnerClass.InnerClass innerClass= declareInnerClass.new InnerClass();
        innerClass.methodDisplayInner();

  }
}