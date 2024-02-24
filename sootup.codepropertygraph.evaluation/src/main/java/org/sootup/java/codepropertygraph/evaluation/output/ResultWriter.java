package org.sootup.java.codepropertygraph.evaluation.output;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ResultWriter {
  private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

  public void writeResultToFile(Path resultFilePath, Map<String, Object> result) {
    try {
      String json = GSON.toJson(result);
      Files.write(resultFilePath, json.getBytes());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
