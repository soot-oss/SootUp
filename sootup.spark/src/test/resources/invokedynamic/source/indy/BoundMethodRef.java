package indy;

class BoundMethodRef {

  public static void main(String[] args) {
    Worker w = new Worker();
    Runnable r = w::work;
    r.run();
  }
}
