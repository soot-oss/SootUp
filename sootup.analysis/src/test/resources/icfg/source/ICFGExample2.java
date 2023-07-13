public class ICFGExample2{
    public ICFGExample2(){
    }

    public void entryPoint(){
        ICFGSuperClass polymorphicObj = new ICFGSubClass();
        polymorphicObj.m();
    }
}

class ICFGSuperClass {
    public void m() {

    }
}

class ICFGSubClass extends ICFGSuperClass {
    @Override
    public void m() {

    }
}