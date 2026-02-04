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
                .apply(JSON_OBJECT);

        // Then
        JSONAssert.assertEquals(EXPECTED_JSON_OBJECT, actual, STRICT);
    }

    @Test
    void shouldMaskNestedObjects() throws JSONException {
        // When
        var actual = JsonMasker.builder()
                .withProperties(Set.of("name", "email", "street", "value"), fixedLength())
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

        var actual = JsonMasker.builder().withProperty("email").apply(givenJson);

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

        // When
        var actual = JsonMasker.builder().withProperties("email", "phone").apply(givenJson);

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

        var actual =
                JsonMasker.builder().withProperties(Set.of("email", "phone")).apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actual, STRICT);
    }

    @Test
    void shouldMaskNestedArrayValues() throws Exception {
        // Given
        String givenJson =
                """
		{
			"name": "John Doe",
			"age": 30,
			"creditCards": [
				"1234-5678-9012-3456",
				"4289-3874-8064-8976"
			]
		}
		""";

        String expectedJson =
                """
		{
			"name": "John Doe",
			"age": 30,
			"creditCards": [
				"XXXX-XXXX-XXXX-3456",
				"XXXX-XXXX-XXXX-8976"
			]
		}
		""";

        var actualJson = JsonMasker.builder()
                .withProperty("creditCards", Masker.creditCard('X'))
                .build()
                .apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actualJson, STRICT);
    }

    @Test
    void shouldMaskMixedTypeArrayValues() throws Exception {
        // Given
        String givenJson =
                """
		{
			"name": "John Doe",
			"mixedArray": [
				"sensitive-string-data",
				42,
				true,
				null,
				{"nestedKey": "nestedValue"},
				["nested", "array"]
			]
		}
		""";

        String expectedJson =
                """
		{
			"name": "John Doe",
			"mixedArray": [
				"*********************",
				42,
				true,
				null,
				{"nestedKey": "***********"},
				["******", "*****"]
			]
		}
		""";

        var actualJson = JsonMasker.builder()
                .withProperty("mixedArray", fixedLength())
                .build()
                .apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actualJson, STRICT);
    }

    @Test
    void shouldMaskNestedArrayStringValues() throws Exception {
        // Given
        String givenJson =
                """
		{
			"name": "John Doe",
			"nestedArrays": [
				["sensitive-outer-inner", "another-value"],
				42,
				["not-masked-1", "not-masked-2"]
			]
		}
		""";
        String expectedJson =
                """
		{
			"name": "John Doe",
			"nestedArrays": [
				["*********************", "*************"],
				42,
				["************", "************"]
			]
		}
		""";

        var actualJson = JsonMasker.builder()
                .withProperty("nestedArrays", fixedLength())
                .build()
                .apply(givenJson);

        // Then
        JSONAssert.assertEquals(expectedJson, actualJson, STRICT);
    }

    @Test
    void shouldRetainExistingSettingsWhenMutating() throws Exception {
        // Given
        var expectedJson =
                """
			{
				"name" : "********",
				"age" : 30,
				"city" : "********",
				"email" : "****************",
				"phone" : "MTIzLTQ1Ni03ODkw"
			}
		""";

        var masker = Masker.json()
                .prettify(true)
                .withProperty("phone", base64())
                .withProperties(fixedLength(), "name", "email")
                .build();

        var maskerCopy = masker.mutate().withProperty("city", fixedLength());

        // When
        var actualJson = maskerCopy.apply(JSON_OBJECT);

        // Then
        JSONAssert.assertEquals(expectedJson, actualJson, STRICT);
    }

    record Person(String name, int age, String city, String email, String phone) {}
}
