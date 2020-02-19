import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

public class AnnotationData {
    private @FutureSootAnnotation
    String str;
    private @FutureSootAnnotation
    List<String> stringlist;
    private @FutureSootAnnotation
    char[] charArray; //on char
    private @FutureSootAnnotation
    char[][] charArray2D;//on char
    private char @FutureSootAnnotation [] charArr;//on char array, char[]
    private char[] @FutureSootAnnotation [] charArrNew; //on char array, char[] which is component of char[][]

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FutureSootAnnotation{}
}
