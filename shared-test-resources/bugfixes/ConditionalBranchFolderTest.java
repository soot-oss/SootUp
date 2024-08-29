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
}