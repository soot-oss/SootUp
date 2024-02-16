package categories;

public enum TestCategories {
  JAVA_8("java8"),
  JAVA_9("java9");

  public static final String JAVA_8_CATEGORY = "java8";
  public static final String JAVA_9_CATEGORY = "java9";

  private final String category;

  TestCategories(String category) {
    this.category = category;
  }

  public String getCategory() {
    return category;
  }
}
