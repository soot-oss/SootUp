package indy;

record Rec(Payload p, int n) {

  public static void main(String[] args) {
    Rec a = new Rec(new Payload(), 1);
    a.toString();
    a.hashCode();
    a.equals(new Rec(new Payload(), 2));
  }
}
