package sootup.callgraph;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Loads ImplicitPatterns.json using Gson. */
public class GsonImplicitPatternsLoader {

  private static final Gson GSON =
      new GsonBuilder()
          .setPrettyPrinting()
          .create();

  /**
   * Load list of ImplicitCallEdge from a JSON file with structure: { "ImplicitPatterns": [ { ... },
   * { ... } ] }
   */
  public static List<ImplicitCallEdge> loadImplicitPatternsFromStream(InputStream in)
      throws IOException {
    try (Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
      JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
      JsonArray patterns = root.getAsJsonArray("implicitPatterns");
      List<ImplicitCallEdge> list = new ArrayList<>();
      for (JsonElement elem : patterns) {
        list.add(GSON.fromJson(elem, FixImplicitCallEdge.class));
      }
      return list;
    }
  }

  // tiny demo main
  public static void main(String[] args) throws Exception {
    System.out.println(
        GsonImplicitPatternsLoader.class.getResource("/Implicit/ImplicitPatterns.json"));
    try (InputStream in =
        GsonImplicitPatternsLoader.class.getResourceAsStream(
            "/Implicit/ImplicitPatterns.json")) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: ImplicitPatterns.json");
      }
      List<ImplicitCallEdge> patterns = loadImplicitPatternsFromStream(in);

      System.out.println("Loaded implicit patterns: " + patterns.size());
      for (ImplicitCallEdge e : patterns) {
        System.out.println("----");
        System.out.println("id: " + e.getId());
        System.out.println("category: " + e.getCategory());
        System.out.println("caller: " + e.getCaller());
        System.out.println("callee: " + e.getCallee());
      }
    }
  }
}
