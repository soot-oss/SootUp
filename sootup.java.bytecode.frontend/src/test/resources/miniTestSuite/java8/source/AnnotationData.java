import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class AnnotationData {
    private @SootUpAnnotation
    String str;
    private @SootUpAnnotation
    List<String> stringlist;
    private @SootUpAnnotation
    char[] charArray; //on char
    private @SootUpAnnotation
    char[][] charArray2D;//on char
    private char @SootUpAnnotation [] charArr;//on char array, char[]
    private char[] @SootUpAnnotation [] charArrNew; //on char array, char[] which is component of char[][]

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SootUpAnnotation{}
}
