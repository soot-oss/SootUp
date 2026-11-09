public class Example {

    /**
     * A simple method used to illustrate live variable analysis.
     *
     * <p>At the join point after the if/else, both the true branch (y = c)
     * and the false branch (y = x) have assigned y, but they use different variables.
     * This makes the join point non-trivial: the set of live variables is the union
     * of what is live on each incoming path.
     */
    public static int compute(int a, int b, int c) {
        int x = a + b;   // stmt: x = a + b  (uses a, b; defines x)
        int y;
        if (a > 0) {
            y = c;       // stmt: y = c       (uses c; defines y)
        } else {
            y = x;       // stmt: y = x       (uses x; defines y)
        }
        return y;        // stmt: return y    (uses y)
    }
}
