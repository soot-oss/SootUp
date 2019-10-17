class DeclareConstructor{

    int var1,var2;

    DeclareConstructor(){
        var1=0;
        var2=0;
        System.out.println("Default Constructor");
    }

    DeclareConstructor(int var1, int var2){
        this.var1=var1;
        this.var2=var2;
        System.out.println("Parameterized Constructor");
    }
}