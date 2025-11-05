package sootup.callgraph;

import com.google.gson.*;
import java.lang.reflect.Type;

/** Custom Deserializer to deserialize implicit patterns of different categories. */
public class ImplicitCallEdgeDeserializer implements JsonDeserializer<ImplicitCallEdge> {
  @Override
  public ImplicitCallEdge deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext context)
      throws JsonParseException {
    JsonObject jsonObject = jsonElement.getAsJsonObject();
    int category = jsonObject.has("category") ? jsonObject.get("category").getAsInt() : -1;
    Class<? extends ImplicitCallEdge> subType =
        switch (category) {
          case 1 -> FixImplicitCallEdge.class;
          case 2 -> IntraImplicitCallEdge.class;
          default -> throw new JsonParseException("Unknown pattern category: " + category);
        };

    return context.deserialize(jsonElement, subType);
  }
}
