package indy;

class Payload {

  Payload self() {
    return this;
  }

  @Override
  public String toString() {
    return "p";
  }

  @Override
  public int hashCode() {
    return 1;
  }

  @Override
  public boolean equals(Object o) {
    return o == this;
  }
}
