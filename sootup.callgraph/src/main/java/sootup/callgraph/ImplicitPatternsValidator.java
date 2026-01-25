package sootup.callgraph;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import java.io.InputStream;

public class ImplicitPatternsValidator {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Validates the ImplicitPatterns.json against its schema.
   * Throws a RuntimeException if validation fails or files are missing.
   */
  public void validateImplicitPatterns() {
    try {
      InputStream schemaStream = getClass().getResourceAsStream("/Implicit/ImplicitPatternsSchema.json");
      InputStream jsonStream = getClass().getResourceAsStream("/Implicit/ImplicitPatterns.json");
      if (schemaStream == null) {
        throw new IllegalStateException("Could not find ImplicitPatternsSchema.json in classpath");
      }
      if (jsonStream == null) {
        throw new IllegalStateException("Could not find ImplicitPatterns.json in classpath");
      }
      JsonNode schemaNode = MAPPER.readTree(schemaStream);
      JsonNode jsonNode = MAPPER.readTree(jsonStream);
      JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
      JsonSchema schema = factory.getSchema(schemaNode);

      Set<ValidationMessage> validationErrors = schema.validate(jsonNode);
      if (!validationErrors.isEmpty()) {
        StringBuilder errorMessage = new StringBuilder("ImplicitPatterns.json is not valid! Errors:\n");
        for (ValidationMessage error : validationErrors) {
          errorMessage.append("- ").append(error.getMessage()).append("\n");
        }
        throw new IllegalStateException(errorMessage.toString());
      }
    } catch (Exception e) {
      if (e instanceof IllegalStateException) {
        throw (IllegalStateException) e;
      }
      throw new RuntimeException("Failed to load or validate ImplicitPatterns configuration", e);
    }
  }
}
