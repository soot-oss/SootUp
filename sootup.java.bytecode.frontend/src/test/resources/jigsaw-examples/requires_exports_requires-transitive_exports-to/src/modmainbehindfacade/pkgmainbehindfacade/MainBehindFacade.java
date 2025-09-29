package pkgmainbehindfacade;

public class MainBehindFacade {
    public static void main(String[] args) {
     // access to pkga1 (exported from moda)
        pkga1.A1 mya1 = new pkga1.A1();
        System.out.println("A1: " + mya1.doIt());

        Object myc1 = mya1.getMyC();
        System.out.println("from A1: getMyC()=" + myc1);

        pkgc.C myc2 = mya1.getMyC();
        System.out.println("from A1: getMyC()=" + myc2);
    }
}
