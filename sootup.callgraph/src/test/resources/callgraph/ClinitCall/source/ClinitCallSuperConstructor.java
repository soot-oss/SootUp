// ccm/Class.java
package ccsc;

class Class {

  public static void main(String[] args){
    Clinit c=new Clinit();
  }
}

class Clinit extends SuperClinit{
  static int a=3;
}

class SuperClinit {
  static int b=3;
}