package ccps;

class ClinitCallPruningSelf {
    public static int x = g();
    public static int g(){return 4;}
}

class Main{
    public static void main(String[] args) {
        int xValue = ClinitCallPruningSelf.x;
    }
}