package de.upb.sootup.concrete.generics;

import java.util.ArrayList;

/**
 * @author Manuel Benz created on 12.07.18
 */
public class Generics {

  public void list() {
    ArrayList<String> list = new ArrayList<>();
    list.add("foo");
    System.out.println(list.get(0));
  }

  public void boxedList() {
    ArrayList<Integer> list = new ArrayList<>();
    list.add(1);
    System.out.println(list.get(0));
  }

  public void nonJDK() {
    A<String> stringA = new A<>();
    stringA.setT("foo");
    System.out.println(stringA.getT());
  }
}
