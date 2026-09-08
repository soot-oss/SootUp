public class Example {

    /**
     * A simple method used to illustrate constant propagation.
     *
     * <p>The variable x is redefined four times before y reads it.
     * A flow-sensitive analysis can determine that x holds the constant 4
     * at the point of the assignment to y, and therefore y = 4 exactly.
     */
    public static void assign() {
        int x = 1;   // OUT: {x=1}
        x = 2;       // OUT: {x=2}
        x = 3;       // OUT: {x=3}
        x = 4;       // OUT: {x=4}
        int y = x;   // OUT: {x=4, y=4}
    }
}
