
/** @author Hasitha Rajapakse */


public class AnonymousClassInsideMethod {

    interface MathOperation {
        public void addition();
    }

    public void anonymousClassInsideMethod() {

        MathOperation myMathOperation = new MathOperation() {
            int i = 0;

            @Override
            public void addition() {
                i++;
            }
        };

        myMathOperation.addition();

    }
}
