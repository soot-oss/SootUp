public class NestedTryCatchFlow {
    int test_nested_try_catch_2(int param) {
        try {
            try {
                try {
                    if (param == 10)
                        return 0;
                    if (param == 20 || param == 25 || param == 27 || param == 28) {
                        throw new IllegalArgumentException("Illegal arguments provided");
                    }
                    if (param == 30) {
                        throw new IllegalStateException("Illegal state exception");
                    }
                    return 1;
                } catch (IllegalArgumentException e) {
                    if (param == 25 || param == 27 || param == 28) {
                        throw new IllegalStateException("Illegal state exception 2");
                    }
                    return 2;
                } catch (IllegalStateException e) {
                    return 3;
                }
            } catch (IllegalStateException e) {
                if (param == 25) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                if (param == 27) {
                    throw new IllegalArgumentException("Illegal arguments provided 2");
                }
                return 4;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            return 5;
        } catch (RuntimeException e) {
            return 6;
        }
    }
}