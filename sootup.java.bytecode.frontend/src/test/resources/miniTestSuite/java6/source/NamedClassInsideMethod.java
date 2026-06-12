
/** @author Hasitha Rajapakse */


public class NamedClassInsideMethod {

    interface MathOperation {
        public void addition();
    }

    public void namedClassInsideMethod() {

        class MyMathOperation implements MathOperation {
            int i = 0;
            public void addition() {
                i++;
            }
        }

        MathOperation myMathOperation = new MyMathOperation();
        myMathOperation.addition();

    }
}
