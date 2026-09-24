
/** @author Kaustubh Kelkar */
class DeclareField {
    public final String s= "Java";
    private static int i=0;

    public void display(){
        System.out.println(s);
    }

    public void staticDisplay(){
        System.out.println(i);
    }

  public static void main(String[] args) {
    DeclareField declareField = new DeclareField();
    declareField.display();
    declareField.staticDisplay();
    //
  }
}