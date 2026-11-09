package de.upb.sootup.instructions.expr;

interface IThing {
  int getId();
}

class Pear implements IThing {

  @Override
  public int getId() {
    return 42;
  }

}

public class InterfaceInvokeExprTest {

  void sth() {

    IThing obj = new Pear();
    int id = obj.getId();
    System.out.println(id);

  }

}
