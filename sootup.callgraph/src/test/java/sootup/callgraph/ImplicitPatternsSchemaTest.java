package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.io.File;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ImplicitPatternsSchemaTest {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void validateImplicitPatternsJson() throws Exception {
    File schemaFile = new File("src/main/resources/Implicit/ImplicitPatternsSchema.json");
    File jsonFile = new File("src/main/resources/Implicit/ImplicitPatterns.json");
    JsonNode schemaNode = MAPPER.readTree(schemaFile);
    JsonNode jsonNode = MAPPER.readTree(jsonFile);
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    JsonSchema schema = factory.getSchema(schemaNode);
    Set<ValidationMessage> validationErrors = schema.validate(jsonNode);
    assertTrue(validationErrors.isEmpty(), "ImplicitPatterns.json is not valid!");
  }
}
