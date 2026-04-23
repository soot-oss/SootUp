package ccp2;

// testClinitCallPruningChild
class ClinitCallPruningParent{
    public static int x = 4;
}

class ClinitCallPruningChild extends ClinitCallPruningParent {
    public static int y = 1;
}

// testClinitCallPruningChild2
class ClinitCallPruningParent2{
    public static int x = 4;
}

class ClinitCallPruningChild2 extends ClinitCallPruningParent2 {
    public static int y = 1;
}

// testClinitCallPruningSelf
class ClinitCallPruningSelf {
    public static int x = g();
    public static int g(){return 4;}
}

// testClinitCallPruningBranch
class ClinitCallPruningBranch {
    // force <clinit> generation
    public static int dummy = 1;
    public static void method() {}
}

// testClinitCallPruningBranch2
class ClinitCallPruningBranch2 {
    public static int dummy2 = 2;
    public static void method2() {}
}

// testClinitCallPruningBranch3
class ClinitCallPruningBranch3 {
    static int value = 100;
}

// testClinitCallPruningBranch4
class ClinitCallPruningBranch4 {
    public static int value2 = 200;
}

class ClinitCallPruning2{
    public static void main(String[] args) {
        // testClinitPruningChild
        ClinitCallPruningParent.x=2;
        ClinitCallPruningChild.y=3;

        // testClinitPruningChild2
        ClinitCallPruningChild2.y=3;
        ClinitCallPruningParent2.x=2;

        // testClinitCallPruningSelf
        int xValue = ClinitCallPruningSelf.x;

        // testClinitCallPruningBranch
        if (args.length > 0) {
            ClinitCallPruningBranch.method();
        } else {
            ClinitCallPruningBranch.method();
        }

        // testClinitCallPruningBranch2
        ClinitCallPruningBranch2.method2();
        if (args.length > 0) {
            ClinitCallPruningBranch2.method2();
        }

        // testClinitCallPruningBranch3
        boolean condition = true;
        int a = ClinitCallPruningBranch3.value;
        while (condition) {
            int b = ClinitCallPruningBranch3.value;
            if (b > 50) {
                condition = false;
            }
        }

        // testClinitCallPruningBranch4
        int c = ClinitCallPruningBranch4.value2;
        if (c > 400) {
            // to create an if-block
            int i = 10;
            i += 10;
        }
        int d = ClinitCallPruningBranch4.value2;
    }
}