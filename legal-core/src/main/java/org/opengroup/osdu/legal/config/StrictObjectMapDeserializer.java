package org.opengroup.osdu.legal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

/**
 * Deserializes a JSON object into {@code Map<String, Object>} and rejects JSON null.
 * Omitted optional fields remain unset; an explicit {@code null} is not an object.
 */
public final class StrictObjectMapDeserializer extends JsonDeserializer<Map<String, Object>> {

    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    @Override
    public Map<String, Object> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {
        if (!parser.hasToken(JsonToken.START_OBJECT)) {
            return (Map<String, Object>) context.handleUnexpectedToken(Map.class, parser);
        }
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        return mapper.readValue(parser, MAP_TYPE);
    }

    @Override
    public Map<String, Object> getNullValue(DeserializationContext context) throws JsonMappingException {
        throw JsonMappingException.from(context.getParser(), "JSON null is not an object");
    }
}
