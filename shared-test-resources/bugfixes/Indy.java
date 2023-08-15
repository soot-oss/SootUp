import java.time.LocalDate;
import java.time.Period;
import java.util.stream.Stream;
import java.util.stream.IntStream;

/** conversion failed when there is a merge (here: after the if) and an invokedynamic followed */
class Indy{
    public IntStream test(IntStream s) {
        int sign;
        if (s.isParallel()) {
            sign = 1;
        }
        return s.map(n -> n+42);
    }
}