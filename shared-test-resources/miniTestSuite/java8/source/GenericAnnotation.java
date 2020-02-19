import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class GenericAnnotation<@AnnotationData.FutureSootAnnotation T> {
    List<@FutureSootAnnotation String> stringList;
    List<@FutureSootAnnotation T> genericList;
    List<@FutureSootAnnotation ? extends Number> genericExtendedList;
    List<@FutureSootAnnotation ? super T> genericSuperList;
    List<@FutureSootAnnotation ?> genericWildcardList1;

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FutureSootAnnotation{}
}