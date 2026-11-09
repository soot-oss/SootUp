
/** @author Kaustubh Kelkar */
public class PrivateMethodInterfaceImpl implements PrivateMethodInterface{
    public void methodInterfaceImpl(){
        methodInterface(4,2);
    }
	
	public static void main(String[] args) {
      PrivateMethodInterfaceImpl privateMethodInterfaceImpl = new PrivateMethodInterfaceImpl();
      privateMethodInterfaceImpl.methodInterfaceImpl();
    }
}