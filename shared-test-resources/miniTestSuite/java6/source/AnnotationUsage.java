@interface OnClass{
  String author() default "";
  int sthBlue() default 123;
}

@interface OnMethod{
  boolean isDuck() default false;
  int sthBorrowed() default 456;
}

@interface OnField{
  boolean isRipe() default false;
  int sthNew() default 789;
}

@interface OnLocal{
  boolean isRubberDuck() default false;
}

@interface OnParameter{
  boolean isBigDuck() default false;
}


@OnClass
public class AnnotationUsage{

  @OnField
  private Object agent;

  @OnMethod
  public AnnotationUsage(){
    // constructor
  }

  @OnMethod
  public void someMethod(@OnParameter boolean isCurvedBanana){
    @OnLocal int i = 0;
  }

}