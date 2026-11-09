package cvcscddi;

class Class implements Interface{

  public static void main(String[] args){
    Class cls = new Class();
    Class cls2 = new SubClass();
    cls.target();
  }
}

class SubClass extends Class implements SubInterface{

  public static void main(String[] args){
    Class cls = new Class();
    cls.target();
  }
}

interface Interface {

  default void target(){ }

}

interface SubInterface extends Interface {

  default void target(){ }

}
