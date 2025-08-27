package prune;

class Pruning{
    public void methodA() {
        this.methodB();
    }

    public void methodB() {
        this.methodC();
    }

    public void methodC() {
    }

    public static void main(String[] args) {
        Pruning p = new Pruning();
        p.methodA();
    }
}