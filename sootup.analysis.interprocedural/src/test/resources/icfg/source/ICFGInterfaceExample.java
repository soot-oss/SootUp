interface MyInterface {
    int calculateSum(int a, int b);
    void displayMessage(String message);
}

class MyClass implements MyInterface {
    @Override
    public int calculateSum(int a, int b) {
        return a + b;
    }

    @Override
    public void displayMessage(String message) {
        String string = "The Message to be displayed is " + message;
    }
}

// Main class to demonstrate the interface methods
public class ICFGInterfaceExample {
    public static void main(String[] args) {
        // Create an instance of MyClass
        MyClass myClass = new MyClass();

        // Call the calculateSum method
        int result = myClass.calculateSum(10, 20);

        // Call the displayMessage method
        myClass.displayMessage("Hello, Interface!");
    }
}
