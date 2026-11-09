import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface MyInnerAnnotation {
    MySecondInnerAnnotation secondInnerAnnotation();
}

@Retention(RetentionPolicy.RUNTIME)
@interface MySecondInnerAnnotation {
    String value();
}

@Retention(RetentionPolicy.RUNTIME)
@interface MyOuterAnnotation {
    MyInnerAnnotation innerAnnotation();
}


@MyOuterAnnotation(innerAnnotation = @MyInnerAnnotation(secondInnerAnnotation = @MySecondInnerAnnotation("second")))
class NestedAnnotation {
    // Class implementation
}