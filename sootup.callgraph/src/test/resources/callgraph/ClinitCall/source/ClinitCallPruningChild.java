package ccpc;

class ClinitCallPruningParent{
    public static int x = 4;
}

class ClinitCallPruningChild extends ClinitCallPruningParent {
    public static int y = 1;


}
class Main{
    public static void main(String[] args) {
        ClinitCallPruningParent.x=2;
        ClinitCallPruningChild.y=3;
    }
}