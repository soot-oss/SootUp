protected class ProtectedClass {
    public void publicMethod() {
        public int a = 10;
        private int b = 20;
        protected int c = 30;
        int d = 40;
    }

    private void privateMethod() {
        public int a = 10;
        private int b = 20;
        protected int c = 30;
        int d = 40;
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
