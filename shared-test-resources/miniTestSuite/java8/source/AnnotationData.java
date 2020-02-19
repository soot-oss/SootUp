public class MyClass {
    private @FutureSootAnnotation
    String str;
    private @FutureSootAnnotation
    List<String> mylist;
    private @FutureSootAnnotation
    char[] chars; //on char
    private @FutureSootAnnotation
    char[][] chars2;//on char
    private char @FutureSootAnnotation [] chars3;//on char array, char[]
    private char[] @FutureSootAnnotation [] chars4; //on char array, char[] which is component of char[][]
}
