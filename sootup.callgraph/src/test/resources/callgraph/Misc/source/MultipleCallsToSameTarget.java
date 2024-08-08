package multi;

class MultipleCallsToSameTarget {

    public static void main(String[] args){
        MultiCalls.method();
        MultiCalls.method();

        FieldLeft.field = 5;
        FieldLeft.field = FieldLeft.method();

        int f = FieldRight.field;
        FieldLeft.field = FieldRight.field;

        Instantiated in1 = new Instantiated();
        Instantiated in2 = new Instantiated();

        in1.method();
        in2.method();
    }
}

class MultiCalls{
    static int field=3;

    static void method(){

    }
}

class FieldLeft{
    static int field=3;
    static void method(){}
}

class FieldRight{
    static int field=3;
}

class Instantiated{
    static int field=3;

    void method(){}
}