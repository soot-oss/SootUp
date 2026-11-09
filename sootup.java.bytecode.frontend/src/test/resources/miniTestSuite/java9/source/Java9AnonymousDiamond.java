import java.util.concurrent.*;

class Java9AnonymousDiamond {
  Callable<String> anonymousDiamond() {
    Callable<String> call = new Callable<>() {
      @Override public String call() {
        return "Hey";
      }
    };
    return call;
  }
}
