package refl;

public class ClassNewInstance {
  public static void main(String[] args) throws Exception {
    Class<?> c = Class.forName(args[0]);
    Service s = (Service) c.newInstance(); // resolved
    s.serve();
  }
}
