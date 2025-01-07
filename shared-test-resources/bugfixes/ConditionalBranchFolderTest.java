public class ConditionalBranchFolderTest {
    void tc1() {
        boolean bool = true;
        if (!bool) {
            System.out.println("False 1");
        } else if (bool) {
            if (bool){
                System.out.println("lets see");
            }
            System.out.println("mid");
        }
        if (!bool) {
            System.out.println("False 2");
        }
    }

    void tc1_1() {
        boolean bool = true;
        if (!bool) {
            System.out.println("False 1");
        } else if (!bool) {
            if (bool){
                System.out.println("lets see");
            }
            System.out.println("mid");
        }
        if (bool) {
            System.out.println("False 2");
        }
    }

    void tc2() {
        boolean bool = true;
        try {
            if (bool) {
                throw new Exception("True");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void tc3() {
        boolean bool = false;
        try {
            if (bool) {
                throw new Exception("True");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void tc4() {
        int x = 10;
        boolean bool = true;
        if(x > 5) {
            try {
                System.out.println("Try Block");
                if (bool) {
                    System.out.println("True inside Try");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println("");
    }

}