public class Issue875_DebugNames {

    void foo(){

        {
            int alpha = 1;
            double trouble = 666;
            System.out.println(alpha);
            System.out.println(trouble);
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

    void cafe(boolean centralPerk){
        System.out.println(centralPerk);
    }

}
