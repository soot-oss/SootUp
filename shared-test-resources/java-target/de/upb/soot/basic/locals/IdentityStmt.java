package de.upb.sootup.basic.locals;

public class IdentityStmt {

  int declProperty;
  int initProperty = 42;

  public void atThis() {

    System.out.println(declProperty);
    System.out.println(initProperty);

  }

  public void atParameter(int a, String str, boolean b) {

    System.out.println(a);
    System.out.println(str);
    System.out.println(b);

  }

  public void atException() {

    // TODO: how to generate ? same as @caughexception?

  }

}