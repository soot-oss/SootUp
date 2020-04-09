import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class AnnotationMethod {


    public String demoMethod() {
        @FutureSootAnnotation List<String> str = new ArrayList<>();
        return "";
    }
    public @FutureSootAnnotation String getString() { return "";}

    public void showString(@FutureSootAnnotation String str) throws  @AnnotationTryCatch.FutureSootAnnotation Exception{}

    <@FutureSootAnnotation number> void demoGenericMethod(){}

    Function<AnnotationMethod, String> f = @FutureSootAnnotation AnnotationMethod::demoMethod;
    public void demoMethod1() throws @FutureSootAnnotation Exception{
        @FutureSootAnnotation List<String> str = new ArrayList<>();
    }

    public static void main(String[] args) {
        AnnotationMethod obj = new AnnotationMethod();
        obj.demoMethod();
        obj.demoGenericMethod();
        try {
            obj.demoMethod1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Target({ElementType.TYPE_USE,ElementType.TYPE_PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface FutureSootAnnotation{}
}