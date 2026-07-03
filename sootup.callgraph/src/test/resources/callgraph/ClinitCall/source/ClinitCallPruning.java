package ccp;

class ClinitCallPruning {

    public enum Operation {

        /** Addition */
        ADD {
            @Override
            public int apply(int a, int b) {
                return a + b;
            }
        },

        /** Subtraction */
        SUBTRACT {
            @Override
            public int apply(int a, int b) {
                return a - b;
            }
        },

        /** Multiplication */
        MULTIPLY {
            @Override
            public int apply(int a, int b) {
                return a * b;
            }
        },

        /** Division – integer division, guards against divide‑by‑zero */
        DIVIDE {
            @Override
            public int apply(int a, int b) {
                if (b == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                return a / b;
            }
        };

        /** Abstract method each constant must implement */
        public abstract int apply(int a, int b);

        /**
         * Utility that parses a textual operator and forwards the call
         * to the corresponding enum constant.
         *
         * @param opSymbol one of "+", "-", "*", "/"
         * @param left     left operand
         * @param right    right operand
         * @return result of the operation
         */
        public static int execute(String opSymbol, int left, int right) {
            switch (opSymbol) {
                case "+":
                    return ADD.apply(left, right);
                case "-":
                    return SUBTRACT.apply(left, right);
                case "*":
                    return MULTIPLY.apply(left, right);
                case "/":
                    return DIVIDE.apply(left, right);
                default:
                    throw new IllegalArgumentException("Unsupported operator: " + opSymbol);
            }
        }
    }
}

class Main {
    public static void main(String[] args) {
        // Hard‑coded sample data
        int a = 12;
        int b = 4;

        ClinitCallPruning.Operation.ADD.apply(a, b);
        ClinitCallPruning.Operation.SUBTRACT.apply(a, b);
        ClinitCallPruning.Operation.MULTIPLY.apply(a, b);
        ClinitCallPruning.Operation.DIVIDE.apply(a, b);

        ClinitCallPruning.Operation.execute("+", a, b);
        ClinitCallPruning.Operation.execute("-", a, b);
        ClinitCallPruning.Operation.execute("*", a, b);
        ClinitCallPruning.Operation.execute("/", a, b);

        // Demonstrate exception handling (optional)
        try {
            ClinitCallPruning.Operation.execute("/", a, 0);
        } catch (ArithmeticException ex) {
            ex.getMessage();
        }
    }
}