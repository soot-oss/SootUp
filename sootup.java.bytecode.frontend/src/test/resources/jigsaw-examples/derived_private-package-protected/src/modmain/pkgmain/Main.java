package pkgmain;

import pkgb.*;

public class Main {
    public static void main(String[] args) {
        Main mymain = new Main();		
        B myb = new B();

// normal call
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt());

// call delegating to internal class
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt_delegateToInternalClass());

// call delegating to internal superclass
        System.out.println("Main: " + mymain.toString() + ", B: " + myb.doIt_delegateToInternalSuperClass());

// calling a protected method (only possible in derived class)
        SubClassFromB subClassFromB = new SubClassFromB(mymain);
        subClassFromB.callInternalSuper();

        // ---------------------------------------------------------------------------------

        DataFactory myDataFactory = new DataFactory();

        System.out.println("Main: " + mymain.toString() + ", Factory.createData(): " + 
                myDataFactory.createData().getName());
        System.out.println("Main: " + mymain.toString() + ", Factory.createInternalDataAsData(): " + 
                myDataFactory.createInternalData1().getName());

// *does* compile though return type of 'DataFactory.createInternalData2()' is not visible here
        System.out.println("Main: " + mymain.toString() + ", Factory.createInternalData2(): " + 
                myDataFactory.createInternalData2());

// does not compile. error: toString() in Object is defined in an inaccessible class or interface
// suprise! why is the statement above ok when adding toString() is not?
// Solution: Concatenation with + uses String.valueOf(Object obj) to turn InternalData into a String
//           This is allowed as it uses the type Object and Object.toString().
//        System.out.println("Main: " + mymain.toString() + ", Factory.createInternalData2().toString(): " + 
//                myDataFactory.createInternalData2().toString());

// does not compile. error: getName() in InternalData is defined in an inaccessible class or interface
//        System.out.println("Main: " + mymain.toString() + ", Factory.createInternalData2().getName(): " + 
//                myDataFactory.createInternalData2().getName());
    }
}

// calling a protected method (only possible in derived class)
class SubClassFromB extends B {
    SubClassFromB(Main mymain) {
        System.out.println("Main: " + mymain.toString() + ", B: " + this.doIt_protected());
    }
    
	// This works despite doItNotOverwritten being defined only in
	// InternalBSuperClass, without being overwritten in B. 
    public void callInternalSuper() {
        System.out.println(doItNotOverwritten());
    }
}
