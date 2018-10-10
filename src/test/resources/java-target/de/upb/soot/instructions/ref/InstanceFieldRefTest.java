package de.upb.soot.instructions.ref;

public class InstanceFieldRefTest {

  int counter = 0;

  void sth() {
    counter++;
    System.out.println(counter);
  }

}