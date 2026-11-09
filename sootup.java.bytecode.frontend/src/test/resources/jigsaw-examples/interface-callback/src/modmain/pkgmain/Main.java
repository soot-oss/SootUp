package pkgmain;

import pkgcallbackhandler.MyCallbackImpl;
import pkgcallee.Callee;

public class Main {
    public static void main(String[] args) {
        Callee callee = new Callee();
        callee.pleaseCall(new MyCallbackImpl());
    }
}
