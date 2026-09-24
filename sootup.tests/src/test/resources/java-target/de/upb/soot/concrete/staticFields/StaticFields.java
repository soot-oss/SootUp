package de.upb.sootup.concrete.staticFields;

public class StaticFields {
  private static final int finalInt = 10;

  private static int nonFinalInt = 5;

  public static void finalField() {
    System.out.println(finalInt);
  }

  public static void nonFinalField() {
    System.out.println(nonFinalInt);
  }

  public static void nonFinalFieldAltered() {
    nonFinalInt++;
    System.out.println(nonFinalInt);
  }

}
