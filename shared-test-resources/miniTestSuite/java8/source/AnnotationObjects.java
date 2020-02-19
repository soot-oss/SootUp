import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AnnotationObjects {

    List<String> list = new @FutureSootAnnotation ArrayList<>();
    AnnotationObjects myClass = new @FutureSootAnnotation AnnotationObjects();
    //AnnotationMethod.NestedClass nestedClass = myClass.new @FutureSootAnnotation NestedClass();
    AnnotationObjects otherClassInstance = (@FutureSootAnnotation AnnotationObjects) myClass;// type casting
    boolean b = myClass instanceof @FutureSootAnnotation Object; //instaceOf check

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FutureSootAnnotation{}
}

