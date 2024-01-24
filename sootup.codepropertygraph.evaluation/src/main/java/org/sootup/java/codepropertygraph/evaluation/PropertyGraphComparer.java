package org.sootup.java.codepropertygraph.evaluation;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class PropertyGraphComparer {

  public PropertyGraphComparer(String joernOutputDirectory, String sootUpOutputDirectory) {

    File joernMethodNamesFile =
        new File(String.format("%s/methodNames.json", joernOutputDirectory));
    File sootUpMethodNamesFile =
        new File(String.format("%s/methodNames.json", sootUpOutputDirectory));

    List<String> joernMethodNames =
        readJsonFile(joernMethodNamesFile, new TypeToken<ArrayList<String>>() {}.getType());
    List<String> sootUpMethodNames =
        readJsonFile(sootUpMethodNamesFile, new TypeToken<ArrayList<String>>() {}.getType());

    // joernMethodNames.forEach(a -> System.out.println("-- " + a));
    // sootUpMethodNames.forEach(a -> System.out.println("## " + a));

    // Todo: Remove this section
    Collections.sort(joernMethodNames);
    Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    try (BufferedWriter writer =
        new BufferedWriter(
            new OutputStreamWriter(
                Files.newOutputStream(joernMethodNamesFile.toPath()), StandardCharsets.UTF_8))) {
      gson.toJson(joernMethodNames, writer);
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (!new HashSet<>(joernMethodNames).containsAll(sootUpMethodNames)) {
      System.out.println("names are matching");
    } else throw new RuntimeException("UNEXPECTED!");
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

}
