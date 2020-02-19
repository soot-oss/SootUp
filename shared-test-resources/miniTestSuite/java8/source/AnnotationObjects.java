public class AnnotationObjects {
    List<String> list = new @FutureSootAnnotation ArrayList<>();
    AnnotationMethod myClass = new @FutureSootAnnotation AnnotationMethod();
    AnnotationMethod.NestedClass nestedClass = myClass.new @FutureSootAnnotation NestedClass();
    AnnotationMethod myClass = (@FutureSootAnnotation AnnotationMethod) otherClassInstance;// type casting
    boolean b = myObject instanceof @FutureSootAnnotation OtherClass; //instaceOf
}