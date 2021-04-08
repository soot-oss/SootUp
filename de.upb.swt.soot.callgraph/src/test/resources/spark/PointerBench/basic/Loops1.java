package basic;

import benchmark.internal.Benchmark;

/*
 * @testcase Loops1
 * 
 * @version 1.0
 * 
 * @author Johannes Späth, Nguyen Quang Do Lisa (Secure Software Engineering Group, Fraunhofer
 * Institute SIT)
 * 
 * @description The analysis must support loop constructs. No allocation site in N
 */
public class Loops1 {

  public class N {
    public String value = "";
    public N next;

    public N() {
      next = null;
    }
  }

  private void test() {
    Benchmark.alloc(1);
    N node = new N();

    int i = 0;
    while (i < 10) {
      node = node.next;
      i++;
    }

    N o = node.next;
    N p = node.next.next;
    N q = node.next.next.next;

    Benchmark
        .test("node",
            "{allocId:1, mayAlias:[node], notMayAlias:[i,o,p,q], mustAlias:[node], notMustAlias:[i,o,p,q]}");
  }

  public static void main(String[] args) {
    Loops1 l1 = new Loops1();
    l1.test();
  }
}
