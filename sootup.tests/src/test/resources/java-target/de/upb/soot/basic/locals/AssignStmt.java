package de.upb.sootup.basic.locals;

import de.upb.sootup.basic.expr.Dummy;

public class AssignStmt {

  int field = 3;

  void sth() {

    // init
    Dummy local = new Dummy();
    int[] arr = new int[666];

    // local -> rvalue ; rvalue : concreteref | imm | expr
    int local11 = field;
    int local12 = local.field;
    int local13 = arr[42];
    int local131 = arr[field];

    int local2 = 42;
    int local3 = Math.abs(-1);

    // field -> imm
    field = 42;
    field = local2;

    // local.field -> dimm
    local.field = 666;
    local.field = field;

    // local [ imm ] ->imm
    arr[0] = 42;
    arr[42] = 123;
    arr[Math.abs(-1)] = 43;
    arr[field] = 0;

  }
}
