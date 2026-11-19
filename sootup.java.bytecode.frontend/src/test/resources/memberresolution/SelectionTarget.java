public class SelectionTarget {}

class SelBase {
  public String n() {
    return "base";
  }
}

class SelSub extends SelBase {
  public String n() {
    return "sub";
  }
}

class SelSubSub extends SelSub {}

interface SelIface {
  default String m() {
    return "iface";
  }
}

class SelPrivate {
  private String m() {
    return "private";
  }
}

// SelPrivate.m is private, so SelIface.m is inherited
class SelPrivateChild extends SelPrivate implements SelIface {}
