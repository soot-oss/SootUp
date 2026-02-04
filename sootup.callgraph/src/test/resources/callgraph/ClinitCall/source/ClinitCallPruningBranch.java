package ccpb;

class ClinitCallPruningBranch {
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

    public static void main(String[] args) {
        // Hard‑coded sample data
        int a = 12;
        int b = 4;

        if (b != 0) {
            Operation.DIVIDE.apply(a, b);
        } else {
            Operation.ADD.apply(a, b);
        }
    }
}