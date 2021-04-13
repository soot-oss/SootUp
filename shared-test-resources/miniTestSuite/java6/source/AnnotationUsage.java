import java.lang.annotation.Retention;
import java.lang.annotation.Repeatable;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Inherited;

@Inherited
@interface OnClass{
  String author() default "";
  int sthBlue() default 123;
}

@interface NonInheritableOnClass{
  int count() default 1;
}

@interface OnMethod{
  boolean isDuck() default false;
  int sthBorrowed() default 456;
}

@interface OnField{
  String isRipe() default "false";
  int sthNew() default 789;
}

@Retention(RetentionPolicy.RUNTIME)
@interface OnLocal{
  boolean isRubberDuck() default false;
}

@interface OnParameter{
  boolean isBigDuck() default false;
}

@Repeatable(OnMethodRepeatables.class)
@interface OnMethodRepeatable{
  int countOnMe() default 0;
}

@interface OnMethodRepeatables{
  OnMethodRepeatable[] value();
}

@NonInheritableOnClass
@OnClass(sthBlue=42, author = "GeorgeLucas")
public class AnnotationUsage{

  @OnField(isRipe = "true")
  private Object agent;

  @OnMethodRepeatable(countOnMe = 1)
  @OnMethodRepeatable(countOnMe = 2)
  public AnnotationUsage(){
    // constructor
  }

  @OnMethod
  public void someMethod(int i, @OnParameter boolean isCurvedBanana, int j, @OnParameter(isBigDuck=true) boolean isDuck){
    @OnLocal
    String s = "(String) Math.random().toString()";
    System.out.println(s);
  }

}