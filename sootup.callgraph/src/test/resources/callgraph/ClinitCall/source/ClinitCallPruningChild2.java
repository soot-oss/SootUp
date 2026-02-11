package ccpc2;

class ClinitCallPruningParent2{
    public static int x = 4;
}

class ClinitCallPruningChild2 extends ClinitCallPruningParent2 {
    public static int y = 1;


}
class Main{
    public static void main(String[] args) {
        ClinitCallPruningChild2.y=3;
        ClinitCallPruningParent2.x=2;
    }
}