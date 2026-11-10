package org.opengroup.osdu.legal.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;

import java.io.IOException;

/**
 * Rejects non-integral JSON numbers for {@code int}/{@link Integer} fields (e.g. {@code 2.5},
 * {@code "10"}, {@code true}, {@code null}).
 */
public final class StrictIntDeserializer extends JsonDeserializer<Integer> {

    @Override
    public Integer deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (!parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return (Integer) context.handleUnexpectedToken(Integer.TYPE, parser);
        }
        return parser.getIntValue();
    }

    @Override
    public Integer getNullValue(DeserializationContext context) throws JsonMappingException {
        throw JsonMappingException.from(context.getParser(), "JSON null is not an integer");
    }
}
