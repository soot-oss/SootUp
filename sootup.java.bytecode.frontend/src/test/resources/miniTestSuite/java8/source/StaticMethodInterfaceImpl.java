class StaticMethodInterfaceImpl implements StaticMethodInterface{

    static public void initStatic(){
        System.out.println("Inside initStatic - StaticmethodInterfaceImpl");
    }
    public void display(){
        System.out.println("Inside display - StaticmethodInterfaceImpl");
    }
    public static void main(String[] args){
        StaticMethodInterface.initStatic();
        initStatic();
        StaticMethodInterfaceImpl obj = new StaticMethodInterfaceImpl();
        obj.display();
    }
}