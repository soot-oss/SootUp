package sootup.callgraph;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.Set;
import org.junit.jupiter.api.Test;

public class ImplicitPatternsTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void giveValidInput() throws Exception {
    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V4);
    JsonSchema schema =
        factory.getSchema(getClass().getResourceAsStream("/Implicit/ImplicitPatterns.json"));
    JsonNode node =
        mapper.readTree(getClass().getResourceAsStream("/Implicit/ImplicitPatternsSchema.json"));
    Set<ValidationMessage> errors = schema.validate(node);
    assertTrue(errors.isEmpty(), "ImplicitPatternsSchema validation failed! Errors. " + errors);
  }
}
