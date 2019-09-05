class NoModifierClass {
    public int a = 10;
    private int b = 20;
    protected int c = 30;

    public void publicMethod() {
        int d = 10;
    }

    private void privateMethod() {
        int d = 20;
    }

    protected void protectedMethod() {
        public int a = 10;
        private int b = 20;
        protected int c = 30;
        int d = 40;
    }

    void noModifierMethod() {
        public int a = 10;
        private int b = 20;
        protected int c = 30;
        int d = 40;
    }
}

