import java.time.LocalDate;
import java.time.Period;
import java.util.stream.LongStream;
import java.util.stream.Stream;
class Indy{
    public Stream<LocalDate> test(LocalDate endExclusive, Period step) {
        long months = step.toTotalMonths();
        long days = step.getDays();
        int sign = months > 0 || days > 0 ? 1 : -1;
        long steps = 200000 / (months + days);
        return LongStream.rangeClosed(0, steps).mapToObj(
                n -> endExclusive.plusDays(days * n));
    }

}