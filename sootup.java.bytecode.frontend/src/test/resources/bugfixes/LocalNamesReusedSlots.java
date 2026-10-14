public class LocalNamesReusedSlots {

  static class Foo {
    String name;

    Foo(String name) {
      this.name = name;
    }

    void use(Foo other) {
      System.out.println(name + other.name);
    }
  }

  static class Bar {
    String name;

    Bar(String name) {
      this.name = name;
    }

    void use(Bar other) {
      System.out.println(name + other.name);
    }
  }

  public static void main(String[] args) {
    System.out.println("start");
    {
      Foo a = new Foo("a");
      Foo b = new Foo("b");
      a.use(b);
      b.use(a);
    }
    System.out.println("middle");
    {
      Bar c = new Bar("c");
      Bar d = new Bar("d");
      c.use(d);
      d.use(c);
    }
    System.out.println("end");
  }
}
