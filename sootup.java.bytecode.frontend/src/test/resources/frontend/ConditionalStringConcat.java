class ConditionalStringConcat {

    public void method(boolean val) {
        String string = "abc" + (val ? "def" : "ghi") + "jkl";
    }
}