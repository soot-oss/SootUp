public @interface AnnotationLibrary{

    String author();
    String genre();
    int currentEdition() default 1;
    String lastIssued() default "N/A";

}
