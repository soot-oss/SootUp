package sootup.callgraph.performance;

public class InvokeFromClassItself {
  private static final int heightZugspitze = 2962;

  public static boolean higherThanZugspitze(int otherHeight) {
    return otherHeight > heightZugspitze;
  }

  public static void main(String[] args) {
    Mountain everest = new Mountain(8848, "Mount Everest");
    System.out.println(higherThanZugspitze(everest.getHeight()));
  }

  static class Mountain {
    public int height;
    public String name;

    public Mountain(int height, String name) {
      this.height = height;
      this.name = name;
    }

    public int getHeight() {
      return height;
    }
  }
}
