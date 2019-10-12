@interface AnnotationLibrary{

    String author();
    String genre();
    int currentEdition() default 1;
    String lastIssued() default "N/A";

}

@AnnotationLibrary(author = "Agatha Christy", genre = "Horror", currentEdition= 5, lastIssued= "01/09/2019");
@AnnotationLibrary(author = "Dan Brown", genre = "Suspense", lastIssued = "01/08/2019");