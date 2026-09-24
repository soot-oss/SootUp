public class BasicInter {

    public static void main(String[] args) {
        O p = new O();
        O q = p;
        O r = new O();
        p.f = r;
        O t = bar(q);
    }

    static O bar(O s){
        return s.f;
    }

    static class O{
        O f;
    }

}
