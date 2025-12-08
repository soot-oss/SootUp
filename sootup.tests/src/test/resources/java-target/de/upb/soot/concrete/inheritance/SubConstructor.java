package de.upb.sootup.concrete.inheritance;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class SubConstructor extends SuperConstructor {
  public SubConstructor() {
    super();
    System.out.println("sub");
  }

  public SubConstructor(String arg) {
    super(arg);
    System.out.println("sub");
  }
}
