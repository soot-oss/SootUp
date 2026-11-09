
/** @author Kaustubh Kelkar */
public class DeclareEnumWithConstructor {
    public enum Number{
        ZERO(0),
        ONE(1),
        TWO(2),
        THREE(3);
        private int value;
        Number(int value){
            this.value=value;
        }
        private int getValue() {
            return value;
        }
    }
    public static void main(String[] args) {
        Number number = Number.ONE;
        System.out.println(number.getValue());
    }
}