package sootup.callgraph;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Loads ImplicitPatterns.json using Gson. */
public class GsonImplicitPatternsLoader {

  private static final Gson GSON =
      new GsonBuilder()
          .registerTypeAdapter(ImplicitCallEdge.class, new ImplicitCallEdgeDeserializer())
          .create();

  /**
   * Load list of ImplicitCallEdge from a JSON file with structure: { "ImplicitPatterns": [ { ... },
   * { ... } ] }
   */
  public static List<ImplicitCallEdge> loadImplicitPatternsFromStream(InputStream in) {
    Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
    JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
    JsonArray patterns = root.getAsJsonArray("implicitPatterns");
    List<ImplicitCallEdge> list = new ArrayList<>();
    for (JsonElement elem : patterns) {
      list.add(GSON.fromJson(elem, ImplicitCallEdge.class));
    }
    return list;
  }

  // tiny demo main TODO: delete in the END
  public static void main(String[] args) throws Exception {
    try (InputStream in =
        GsonImplicitPatternsLoader.class.getResourceAsStream("/Implicit/ImplicitPatterns.json")) {
      if (in == null) {
        throw new FileNotFoundException("Resource not found: ImplicitPatterns.json");
      }
      List<ImplicitCallEdge> patterns = loadImplicitPatternsFromStream(in);
      for (ImplicitCallEdge e : patterns) {
        System.out.println("Implicit call edge " + e);
        System.out.println("Category: " + e.getCategory());
      }
    }
  }
}
