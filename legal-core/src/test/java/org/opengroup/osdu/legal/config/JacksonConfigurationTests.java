package org.opengroup.osdu.legal.config;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.opengroup.osdu.legal.tags.dto.CreateLegalTag;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opengroup.osdu.legal.util.JsonTestUtils.json;
import static org.opengroup.osdu.legal.util.JsonTestUtils.withField;
import static org.opengroup.osdu.legal.util.LegalTagRequestTestData.createBody;

public class JacksonConfigurationTests {

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        JacksonConfiguration.applyStrictStringCoercion(objectMapper);
    }

    @Test
    public void should_rejectBooleanCoercionToString_onCreateLegalTagName() {
        assertThrows(JsonMappingException.class, () -> objectMapper.readValue(json(withField(createBody(), "name", false)), CreateLegalTag.class));
    }

    @Test
    public void should_rejectBooleanCoercion_whenAppliedViaBuilderCustomizer() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        new JacksonConfiguration().strictStringCoercionCustomizer().customize(builder);

        assertThrows(JsonMappingException.class, () -> builder.build().readValue(json(withField(createBody(), "name", false)), CreateLegalTag.class));
    }

    @Test
    public void should_acceptStringValues() throws Exception {
        CreateLegalTag request = createBody();
        request.setName("abc");
        CreateLegalTag created = objectMapper.readValue(json(request), CreateLegalTag.class);

        assertEquals("abc", created.getName());
    }
}
