interface ResIface {
  int CONST = 1;

  default String defaultMethod() {
    return "default";
  }

  String abstractMethod();
}

class ResBase implements ResIface {
  String field;

  public String abstractMethod() {
    return "base";
  }

  void inherited() {}
}

class ResChild extends ResBase {}

abstract class ResAbstract implements ResIface {}

public class MemberResolutionTarget {}

interface ResOuter {
  default String pick() {
    return "outer";
  }
}

interface ResInner extends ResOuter {
  default String pick() {
    return "inner";
  }
}

// ResOuter comes first, but ResInner.pick overrides ResOuter.pick
class ResPick implements ResOuter, ResInner {}

interface ResUtil {
  static String util() {
    return "static";
  }

  private String hidden() {
    return "private";
  }
}

class ResUtilUser implements ResUtil {}
