class ForLoopSSA {
    public static void main(String[] args) {
        String input = "";
        for (int i = 0; i < args.length; i++) {
            input = input + args[i];
        }
        System.out.println(input);
    }
}