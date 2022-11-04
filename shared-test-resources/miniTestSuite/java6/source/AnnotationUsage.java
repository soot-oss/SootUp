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
  String containerValue() default "defaultValue";
  OnMethodRepeatable[] value() default {@OnMethodRepeatable(countOnMe = 1337)};
}

@interface ArrayConstant{
  String [] value() default {"first","second"};
}

@interface EnumAnnotation{
  AnnotationUsage.Enums[] array() default {AnnotationUsage.Enums.ENUM1,AnnotationUsage.Enums.ENUM2};
  AnnotationUsage.Enums single() default AnnotationUsage.Enums.ENUM3;
}

@interface ClassAnnotation{
  Class[] array() default {Boolean.class,Double.class};
  Class single() default Integer.class;
}

@NonInheritableOnClass
@OnClass(sthBlue=42, author = "GeorgeLucas")
public class AnnotationUsage{

  public static enum Enums{
    ENUM1,
    ENUM2,
    ENUM3
  }

  @OnField(isRipe = "true")
  private Object agent;
  @ArrayConstant
  private Object arrayDefault;
  @EnumAnnotation
  private Object enumDefault;
  @ClassAnnotation
  private Object classDefault;

  @OnMethodRepeatables(containerValue = "betterValue", value = {
      @OnMethodRepeatable(countOnMe = 42)
  })
  public AnnotationUsage(){
    // constructor
  }

  @OnMethodRepeatable(countOnMe=1)
  @OnMethodRepeatable(countOnMe=2)
  public void anotherMethod() {

  }

  @OnMethod
  public void someMethod(int i, @OnParameter boolean isCurvedBanana, int j, @OnParameter(isBigDuck=true) boolean isDuck){
    @OnLocal
    String s = "(String) Math.random().toString()";
    System.out.println(s);
  }

  @ArrayConstant({"test","test1"})
  public void arrayConstant(){

  }

  @EnumAnnotation(array = {AnnotationUsage.Enums.ENUM3,AnnotationUsage.Enums.ENUM2}, single=AnnotationUsage.Enums.ENUM1)
  public void enums(){

  }
  @ClassAnnotation(array = {Integer.class,String.class}, single=Double.class)
  public void classes(){

  }
}