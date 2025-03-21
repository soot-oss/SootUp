package issue903;
import java.io.Closeable;

interface A extends Closeable {
  @Override
  default void close(){
    close();
  }
}

class B implements A {
  public static void main(String[] args) {
    try (B b = new B()) {
    }
  }
}