public class Issue875_DebugNames {

    void foo(){

        {
            int alpha = 1;
            System.out.println(alpha);
        }

        {
            int beta = 2;
            System.out.println(beta);
        }

        {
            int gamma = 3;
            System.out.println(gamma);
        }

    }

}
