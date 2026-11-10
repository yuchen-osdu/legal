package org.opengroup.osdu.legal.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Applies strict JSON type coercion as a baseline for request bodies.
 *
 * <p>LegalTag create/update/query DTOs also use {@link StrictStringDeserializer} /
 * {@link StrictIntDeserializer} / {@link CreateLegalTagPropertiesDeserializer} so rejection does
 * not depend on this ObjectMapper-wide config being wired into the MVC converter.
 */
@Configuration
public class JacksonConfiguration {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer strictStringCoercionCustomizer() {
        return builder -> builder.postConfigurer(JacksonConfiguration::applyStrictStringCoercion);
    }

    public static void applyStrictStringCoercion(ObjectMapper objectMapper) {
        objectMapper.disable(DeserializationFeature.ACCEPT_FLOAT_AS_INT);
        objectMapper.coercionConfigFor(String.class)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Integer, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyArray, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.EmptyObject, CoercionAction.Fail);
        objectMapper.coercionConfigFor(Integer.class)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
        objectMapper.coercionConfigFor(Integer.TYPE)
                .setCoercion(CoercionInputShape.String, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Float, CoercionAction.Fail)
                .setCoercion(CoercionInputShape.Boolean, CoercionAction.Fail);
    }
}
