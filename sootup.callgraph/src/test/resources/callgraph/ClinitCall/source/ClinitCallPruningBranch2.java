package ccpb2;

class ClinitCallPruningBranch2 {
    public enum Operation {
        ADD {
            @Override
            public int apply(int a, int b) {
                return a + b;
            }
        },
        DIVIDE {
            @Override
            public int apply(int a, int b) {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            }
        };

        public abstract int apply(int a, int b);
    }
}

class Main {
    public static void main(String[] args) {
        // Hard‑coded sample data
        int a = 12;
        int b = 4;
        ClinitCallPruningBranch2.Operation.ADD.apply(a, b);
        if (b != 0) {
            ClinitCallPruningBranch2.Operation.DIVIDE.apply(a, b);
        }
    }
}