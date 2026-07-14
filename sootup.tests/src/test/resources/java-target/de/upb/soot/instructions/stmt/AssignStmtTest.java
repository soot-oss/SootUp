package de.upb.sootup.instructions.stmt;

import de.upb.sootup.basic.expr.Dummy;

public class AssignStmtTest {

  int field = 123;
  Dummy fieldObj = new Dummy();

  // local = rvalue; rvalue : concreteref | imm | expr
  public void rvalue() {

    Dummy local = new Dummy();
    int[] arr = new int[123];

    int local11 = field;
    int local12 = local.field;
    int local13 = arr[42];
    int local131 = arr[field];

    int local2 = 42;
    int local3 = Math.abs(-1);

    System.out.println(local);
    System.out.println(arr);
    System.out.println(local11);
    System.out.println(local12);
    System.out.println(local13);
    System.out.println(local131);
    System.out.println(local2);
    System.out.println(3);

  }

  // field = imm
  public void imm() {

    int local2 = 2;
    field = 42;

    Dummy local = new Dummy();
    local.field = local2;

    System.out.println(local2);
    System.out.println(field);
    System.out.println(local.field);

  }

  // local.field = imm
  public void property() {

    Dummy local = new Dummy();
    local.field = 666;

    Dummy local2 = new Dummy();
    local2.field = field;

    fieldObj.field = 42;

    System.out.println(local.field);
    System.out.println(local2.field);
    System.out.println(fieldObj.field);

  }

  // local[immm] = imm;
  public void array_field() {

    int[] arr = new int[123];
    arr[0] = 1;
    arr[42] = 43;
    arr[Math.abs(-1)] = -2;
    arr[field] = 0;

  }

}
