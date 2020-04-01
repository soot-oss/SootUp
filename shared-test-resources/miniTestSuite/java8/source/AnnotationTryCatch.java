import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class AnnotationTryCatch {
    AnnotationTryCatch obj = new @FutureSootAnnotation AnnotationTryCatch();

    public String demoMethod()throws  @FutureSootAnnotation Exception{
        return "";
    }




    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FutureSootAnnotation{}

}