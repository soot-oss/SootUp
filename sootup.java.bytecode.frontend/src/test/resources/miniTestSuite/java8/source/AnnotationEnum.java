import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AnnotationEnum {

    public enum Day {
        Monday,
        Tuesday,
        Wednesday,
        Thursday,
        Friday,
        @Weekend
        Saturday,
        @Weekend
        Sunday
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Weekend {}
}
