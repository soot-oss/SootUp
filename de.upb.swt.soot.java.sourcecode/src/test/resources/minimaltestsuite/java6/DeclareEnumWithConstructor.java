
/** @author: Kaustubh Kelkar */


public enum DeclareEnumWithConstructor {
    //public enum Number{
        ZERO(0),
        ONE(1),
        TWO(2),
        THREE(3);

    private int value;

    private void DeclareEnumWithConstructor(int value){
        this.value=value;
    }


    private int getValue() {
        return value;
    }

        public static void main(String[] args) {
            DeclareEnumWithConstructor number = DeclareEnumWithConstructor.TWO;
            System.out.println(number.getValue());
        }
    }
//}