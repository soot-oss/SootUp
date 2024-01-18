package org.sootup.java.codepropertygraph.evaluation;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import sootup.java.core.JavaIdentifierFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PropertyGraphComparer {
  private int numberOfDifferentMethods;

  public PropertyGraphComparer(String joernOutputDirectory, String sootUpOutputDirectory) {

    File joernMethodNamesFile = new File(String.format("%s/methodNames.json", joernOutputDirectory));
    File sootUpMethodNamesFile = new File(String.format("%s/methodNames.json", sootUpOutputDirectory));

    List<String> joernMethodNames = readJsonFile(joernMethodNamesFile, new TypeToken<ArrayList<String>>(){}.getType());
    List<String> sootUpMethodNames = readJsonFile(sootUpMethodNamesFile, new TypeToken<ArrayList<String>>(){}.getType());

    joernMethodNames.forEach(a -> System.out.println("-- " + a));
    sootUpMethodNames.forEach(a -> System.out.println("## " + a));
  }

  private static <T> ArrayList<T> readJsonFile(File file, Type listType) {
    Gson gson = new Gson();

    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
      return gson.fromJson(reader, listType);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }

  public int getNumberOfDifferentMethods() {
    return numberOfDifferentMethods;
  }
}
