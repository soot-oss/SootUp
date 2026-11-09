
/** @author Kaustubh Kelkar */
abstract class InstanceOfCheckSuper{

}
class InstanceOfCheck extends InstanceOfCheckSuper{

    public void instanceOfCheckMethod(){
        InstanceOfCheck obj= new InstanceOfCheck();
        System.out.println(obj instanceof InstanceOfCheckSuper);
    }
}