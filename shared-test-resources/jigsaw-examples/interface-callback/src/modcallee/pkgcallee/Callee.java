package pkgcallee;

public class Callee {	
    public void pleaseCall(ICallback cb) {
        cb.doCallback("Callee is calling back as requested...");
    }
}
