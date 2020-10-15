/***@author Kaustubh Kelkar*/

class CastingInNumTypes{

    public void displayNum(){
        byte num1 =1;
        short num2=2;
        int num3= 3;
        long num4=4551598461l;
        float num5= 5.4f;
        double num6= 4551595484654646464654684664646846713431.265;

        System.out.println(num1);
        System.out.println((byte)num3);
        System.out.println((double)num2);
        System.out.println((short)num4);
        System.out.println((double)num5);
        System.out.println((int)num4);
        System.out.println((float) num6);
        System.out.println(num6);

        double d = 4786777867867868654674678346734763478673478654478967.77;
        System.out.println((float)d);
        System.out.println((long)d);
        System.out.println((int)d);
        System.out.println((short)d);
        System.out.println((byte)d);

    }

  public static void main(String[] args) {
    CastingInNumTypes castingInNumTypes = new CastingInNumTypes();
    castingInNumTypes.displayNum();
  }
}