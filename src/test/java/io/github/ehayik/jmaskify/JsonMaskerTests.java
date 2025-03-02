package io.github.ehayik.jmaskify;

import static io.github.ehayik.jmaskify.Masker.base64;
import static io.github.ehayik.jmaskify.Masker.fixedLength;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Set;
import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoSettings;
import org.skyscreamer.jsonassert.JSONAssert;

@MockitoSettings
class JsonMaskerTests {

    private static final String JSON_OBJECT =
            """
				{
				"name": "John Doe",
				"age": 30,
				"city": "New York",
				"email": "john@example.com",
				"phone": "123-456-7890"
				}
		""";

    private static final String EXPECTED_JSON_OBJECT =
            """
{
"name" : "********",
"age" : 30,
"city" : "New York",
"email" : "****************",
"phone" : "MTIzLTQ1Ni03ODkw"
}
""";

    private static final String JSON_NESTED_OBJECT =
            """
			{
					"name": "John Doe",
					"age": 30,
					"city": "New York",
					"email": "john@example.com",
					"phone": "123-456-7890",
					"address": {
							"street": "123 Main St",
							"state": "NY",
							"zip": "10001"
					},
					"contacts": [
							{
									"type": "email",
									"value": "john.doe@example.com"
							},
							{
									"type": "phone",
									"value": "555-555-5555"
							}
					]
			}
		""";

    private static final String EXPECTED_JSON_NESTED_OBJECT =
            """
{"name":"********","age":30,"city":"New York","email":"****************","phone":"123-456-7890","address":{"street":"***********","state":"NY","zip":"10001"},"contacts":[{"type":"email","value":"********************"},{"type":"phone","value":"************"}]}
""";

    @Mock
    private ObjectMapper objectMapper;

    @Test
    void shouldMaskJsonContent() throws JSONException {
        // When
        var actual = Masker.json()
                .prettify(true)
                .withProperty("phone", base64())
                .withProperties(fixedLength(), "name", "email")
                .build()
                .apply(JSON_OBJECT);

        // Then
        JSONAssert.assertEquals(EXPECTED_JSON_OBJECT, actual, STRICT);
    }

    @Test
    void shouldMaskNestedObjects() throws JSONException {
        // When
        var actual = JsonMasker.builder()
                .withProperties(Set.of("name", "email", "street", "value"), fixedLength())
                .build()
                .apply(JSON_NESTED_OBJECT);

        // Then
        JSONAssert.assertEquals(EXPECTED_JSON_NESTED_OBJECT, actual, STRICT);
    }

    @Test
    void shouldMaskObject() throws Exception {
        // Given
        var person = new ObjectMapper().readValue(JSON_OBJECT, Person.class);

        // When
        var actual = Masker.json()
                .prettify(true)
                .withProperty("phone", base64())
                .withProperties(fixedLength(), "name", "email")
                .build()
                .applyToObject(person);

        // Then
        JSONAssert.assertEquals(EXPECTED_JSON_OBJECT, actual, STRICT);
    }

    @Test
    void shouldThrowExceptionWhenObjectInvalid() throws Exception {
        // Given
        var masker = Masker.json()
                .prettify(true)
                .withObjectMapper(objectMapper)
                .withProperty("phone", base64())
                .withProperties(fixedLength(), "name", "email")
                .build();

        given(objectMapper.writeValueAsString(any())).willThrow(new RuntimeException("Error"));

        // When - Then
        assertThatThrownBy(() -> masker.applyToObject(List.of()))
                .isInstanceOf(MaskingException.class)
                .hasMessage("Failed to deserialize input value as JSON content");
    }

    @Test
    void shouldThrowExceptionWhenContentInvalid() {
        // Given
        var masker = Masker.json()
                .prettify(true)
                .withProperty("phone", base64())
                .withProperties(fixedLength(), "name", "email")
                .build();

        // When - Then
        assertThatThrownBy(() -> masker.apply("///"))
                .isInstanceOf(MaskingException.class)
                .hasMessage("Failed to mask JSON content");
    }

    @Test
    void shouldMaskJsonUsingDefaultPropertyMasker() throws Exception {
        // Given
        var givenJson = """
		{"email":"email","phone":"123-456-7890"}
		""";
        var expectedJson = """
		{"email":"*****","phone":"123-456-7890"}
		""";

        var actual = JsonMasker.builder().withProperty("email").build().apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actual, STRICT);
    }

    @Test
    void shouldMaskJsonUsingDefaultPropertiesMasker() throws Exception {
        // Given
        var givenJson = """
		{"email":"email","phone":"123-456-7890"}
		""";
        var expectedJson = """
		{"email":"*****","phone":"************"}
		""";

        var actual =
                JsonMasker.builder().withProperties("email", "phone").build().apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actual, STRICT);
    }

    @Test
    void shouldMaskJsonUsingDefaultPropertiesSetMasker() throws Exception {
        // Given
        var givenJson = """
		{"email":"email","phone":"123-456-7890"}
		""";
        var expectedJson = """
		{"email":"*****","phone":"************"}
		""";

        var actual = JsonMasker.builder()
                .withProperties(Set.of("email", "phone"))
                .build()
                .apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actual, STRICT);
    }

    record Person(String name, int age, String city, String email, String phone) {}
}
