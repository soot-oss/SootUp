package de.upb.sootup.instructions.javabytecode.stmt;

public class SwitchStmtsTest {

  public void tableSwitch(int a) {
    switch (a) {
      case 0:
        System.out.println("zero");
      case 1:
        System.out.println("one");
      case 2:
        System.out.println("two");
      case 3:
        System.out.println("three");
      case 4:
        System.out.println("four");
    }
  }

  public void tableSwitchWithDefault(int a) {
    switch (a) {
      case 0:
        System.out.println("zero");
      case 1:
        System.out.println("one");
      case 2:
        System.out.println("two");
      case 3:
        System.out.println("three");
      case 4:
        System.out.println("four");
      default:
        System.out.println("unspecified");
    }
  }

  public void tableSwitchWithBreakEveryOtherCase(int a) {
    switch (a) {
      case 0:
        System.out.println("zero");
      case 1:
        System.out.println("one");
        break;
      case 2:
        System.out.println("two");
      case 3:
        System.out.println("three");
        break;
      case 4:
        System.out.println("four");
    }
  }

  public void lookupSwitch(int a) {
    switch (a) {
      case 1:
        System.out.println("one");
      case 10:
        System.out.println("ten");
      case 1000:
        System.out.println("thousand");
      case 10000000:
        System.out.println("a lot");
    }
  }

  public void lookupSwitchWithDefault(int a) {
    switch (a) {
      case 1:
        System.out.println("one");
      case 10:
        System.out.println("ten");
      case 1000:
        System.out.println("thousand");
      case 10000000:
        System.out.println("a lot");
      default:
        System.out.println("unspecified");
    }
  }

  public void lookupSwitchWithBreakEveryOtherCase(int a) {
    switch (a) {
      case 1:
        System.out.println("one");
      case 10:
        System.out.println("ten");
        break;
      case 1000:
        System.out.println("thousand");
      case 10000000:
        System.out.println("a lot");
        break;
      default:
        System.out.println("unspecified");
    }
  }

}
