package categories;

public enum TestCategories {
  JAVA_8("Java8"),
  JAVA_9("Java9");

  public static final String JAVA_8_CATEGORY = "Java8";
  public static final String JAVA_9_CATEGORY = "Java9";

  private final String category;

  TestCategories(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }
}
