public class AnnotationMethod {
    public @FutureSootAnnotation
    String getString() {.....}

    public void showString(@FutureSootAnnotation String str) {.....}

    myObject .

    <@FutureSootAnnotation String> myMethod(.....)

    public void myMethod() {
        @FutureSootAnnotation List<String> str = new ArrayList<>();
    }

    Function<AnnotationMethod, String> f = @FutureSootAnnotation AnnotationMethod::myMethod;
}