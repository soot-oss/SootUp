public class Reassignment {

  public void calculate() {
    // Initialize variables
    int a = 5;
    int b = 10;
    int c = 15;

    // Perform some operations and reassignments
    a = a + b; // a depends on its previous value and b
    b = a - c; // b now depends on new a and c
    c = b * 2; // c depends on new b

    // More reassignments
    int d = a; // d depends on current a
    a = c + d; // a depends on current c and new d
    b = d - a; // b depends on new d and new a

    // Final reassignment
    c = a + b + d; // c depends on new a, new b, and d
  }
}
