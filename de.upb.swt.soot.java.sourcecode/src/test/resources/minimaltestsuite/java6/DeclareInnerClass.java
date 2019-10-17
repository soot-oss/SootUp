class DeclareInnerClass{
    public int a=1;
    public DeclareInnerClass getDeclareInnerClass(){
        return DeclareInnerClass.this;
    }
    public void methodDisplayOuter(){
        System.out.println("methodDisplayOuter");
    }

    class InnerClass{
        public void methodDisplayInner(){
            System.out.println("methodDisplayInner");
        }

    InnerClass innerClass = new InnerClass();
        innerClass.methodDisplayInner();
    }

  public static void main(String[] args) {
        DeclareInnerClass declareInnerClass = new DeclareInnerClass();
        declareInnerClass.methodDisplayOuter();
  }
}