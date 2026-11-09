import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AnnotationObjects {

    List<String> list = new @SootUpAnnotation ArrayList<>();
    AnnotationObjects myClass = new @SootUpAnnotation AnnotationObjects();
    //AnnotationMethod.NestedClass nestedClass = myClass.new @SootUpAnnotation NestedClass();
    AnnotationObjects otherClassInstance = (@SootUpAnnotation AnnotationObjects) myClass;// type casting
    boolean b = myClass instanceof @SootUpAnnotation Object; //instaceOf check

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SootUpAnnotation{}
}

