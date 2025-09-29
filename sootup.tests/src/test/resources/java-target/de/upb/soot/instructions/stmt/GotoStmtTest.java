package de.upb.sootup.instructions.stmt;

public class GotoStmtTest {

  public void label() {

    label1: while (true) {
      continue label1;
    }

  }

  public void whileloop(int a) {

    while (a < 42) {
      System.out.println("A");
    }

  }

  public void forloop() {

    for (int i = 5; i < 10; i++) {
      System.out.println("A");
    }

  }

  public void dowhileloop(int i) {

    do {
      System.out.println("A");
    } while (i != 0);

  }

}
