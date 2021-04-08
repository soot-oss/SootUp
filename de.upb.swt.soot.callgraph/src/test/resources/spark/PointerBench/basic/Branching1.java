import objects.A;

public class Branching1 {

    public static void main(String[] args) {
        int i = 0;

        A a = new A();
        A b = new A();

        if (i < 0)
            a = b;

//        Benchmark.test("a",
//                "{allocId:1, mayAlias:[a], notMayAlias:[i,b], mustAlias:[a], notMustAlias:[i,b]},"
//                        + "{allocId:2, mayAlias:[a,b], notMayAlias:[i], mustAlias:[a], notMustAlias:[i,b]}");
    }
}