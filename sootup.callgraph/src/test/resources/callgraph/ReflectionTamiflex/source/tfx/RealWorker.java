package tfx;

public class RealWorker implements Worker {
  public RealWorker() {}

  public Object work(Object in) {
    return in;
  }
}
