package ccp2;

class ClinitCallPruning2 {

    public enum Directions {

        NORTH,
        EAST,
        SOUTH,
        WEST;

        /** Prints the name of *this* direction. */
        public void print() {
            // `name()` returns the identifier used for the enum constant
            System.out.println(name());
        }

        /** Prints all directions, one per line. */
        public static void printAll() {
            for (Directions d : Directions.values()) {
                d.print();          // re‑uses the same `print()` implementation
            }
        }
    }

    public static void main(String[] args) {
        // Demonstrate exception handling (optional)
        try {
            Directions.printAll();
        } catch (Exception ex) {
            System.out.println("\nCaught expected exception: " + ex.getMessage());
        }
    }
}