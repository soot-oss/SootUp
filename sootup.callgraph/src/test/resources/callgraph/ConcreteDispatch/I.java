/** @author: Jonas Klauke **/

public interface I extends J {
  default void interfaceMethod(){
    int num = 3;
  }
}