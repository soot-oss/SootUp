package ccpc;

class ClinitCallPruningParent{
    static {}
}

class ClinitCallPruningChild extends ClinitCallPruningParent {
    static {}

    public static void main(String[] args) {
        ClinitCallPruningChild child = new ClinitCallPruningChild();
    }
}