package org.opengroup.osdu.legal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.opengroup.osdu.core.common.model.legal.Properties;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Deserializes create-request {@link Properties} without Jackson coercing non-strings to
 * strings (e.g. {@code "originator": false} → {@code "false"}). Explicit JSON null for
 * {@code extensionProperties} is also rejected (schema type is object, not nullable).
 *
 * <p>Unknown property names are rejected so create matches OpenAPI
 * {@code CreateLegalTagProperties.additionalProperties: false}. Shared {@link Properties}
 * keeps {@code @JsonIgnoreProperties(ignoreUnknown = true)} for response/persistence paths.
 */
public final class CreateLegalTagPropertiesDeserializer extends JsonDeserializer<Properties> {

    private static final String EXTENSION_PROPERTIES = "extensionProperties";
    private static final String COUNTRY_OF_ORIGIN = "countryOfOrigin";

    private static final List<String> STRING_PROPERTIES = List.of(
            "contractId",
            "originator",
            "dataType",
            "securityClassification",
            "personalData",
            "exportClassification",
            "expirationDate");

    /** Declared fields on {@link Properties} / CreateLegalTagProperties. */
    private static final Set<String> ALLOWED_PROPERTIES;

    static {
        Set<String> allowed = new HashSet<>(STRING_PROPERTIES);
        allowed.add(COUNTRY_OF_ORIGIN);
        allowed.add(EXTENSION_PROPERTIES);
        ALLOWED_PROPERTIES = Set.copyOf(allowed);
    }

    @Override
    public Properties deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.START_OBJECT)) {
            return (Properties) context.handleUnexpectedToken(Properties.class, parser);
        }
        ObjectMapper mapper = (ObjectMapper) parser.getCodec();
        JsonNode node = mapper.readTree(parser);
        rejectUnknownProperties(parser, node);
        rejectInvalidPropertyTypes(parser, node);
        return mapper.treeToValue(node, Properties.class);
    }

    @Override
    public Properties getNullValue(DeserializationContext context) throws JsonMappingException {
        throw JsonMappingException.from(context.getParser(), "JSON null is not an object");
    }

    private static void rejectUnknownProperties(JsonParser parser, JsonNode node)
            throws JsonMappingException {
        Iterator<String> names = node.fieldNames();
        while (names.hasNext()) {
            String name = names.next();
            if (!ALLOWED_PROPERTIES.contains(name)) {
                throw JsonMappingException.from(parser, "Unrecognized field \"" + name + "\"");
            }
        }
    }

    private static void rejectInvalidPropertyTypes(JsonParser parser, JsonNode node)
            throws JsonMappingException {
        for (String property : STRING_PROPERTIES) {
            if (!node.has(property)) {
                continue;
            }
            if (!node.get(property).isTextual()) {
                throw JsonMappingException.from(parser,
                        "JSON value is not a string for property '" + property + "'");
            }
        }
        if (node.has(COUNTRY_OF_ORIGIN)) {
            JsonNode countryOfOrigin = node.get(COUNTRY_OF_ORIGIN);
            if (!countryOfOrigin.isArray()) {
                throw JsonMappingException.from(parser,
                        "JSON value is not an array for property '" + COUNTRY_OF_ORIGIN + "'");
            }
            for (JsonNode item : countryOfOrigin) {
                if (!item.isTextual()) {
                    throw JsonMappingException.from(parser,
                            "JSON value is not a string for property '" + COUNTRY_OF_ORIGIN + "' items");
                }
            }
        }
        if (node.has(EXTENSION_PROPERTIES) && !node.get(EXTENSION_PROPERTIES).isObject()) {
            throw JsonMappingException.from(parser,
                    "JSON value is not an object for property '" + EXTENSION_PROPERTIES + "'");
        }
    }
}
