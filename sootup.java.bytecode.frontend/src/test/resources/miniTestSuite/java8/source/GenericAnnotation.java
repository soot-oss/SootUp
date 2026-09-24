import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class GenericAnnotation<@AnnotationData.SootUpAnnotation T> {
    List<@SootUpAnnotation String> stringList;
    List<@SootUpAnnotation T> genericList;
    List<@SootUpAnnotation ? extends Number> genericExtendedList;
    List<@SootUpAnnotation ? super T> genericSuperList;
    List<@SootUpAnnotation ?> genericWildcardList1;

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SootUpAnnotation{}
}