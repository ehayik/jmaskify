package io.github.ehayik.jmaskify;

import static com.fasterxml.jackson.core.JsonToken.*;
import static java.util.Objects.requireNonNullElseGet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Responsible for masking specified fields in a JSON string using customizable masking strategies.
 *
 * <p>
 *   This class is immutable and thread-safe. It uses Jackson Stream API for JSON processing,
 *   ensuring better performance during serialization and deserialization of JSON content.
 *
 * <p>Usage example:
 * <pre>{@code
 * String json = """
 * {
 *     "name": "John Doe",
 *     "age": 30,
 *     "email": "john.doe@example.com",
 *     "phone": "123-456-7890"
 * }
 * """;
 *
 * // Create a JsonMasker instance with masking strategies for specific fields
 * JsonMasker jsonMasker = JsonMasker.builder()
 *     .withProperty("name") // Mask the name field with a fixed pattern
 *     .withProperty("email") // Mask the email field with a fixed pattern
 *     .withProperty("phone", Masker.base64()) // Mask the phone field using Base64 encoding
 *     .prettify(true) // Enable pretty printing
 *     .build();
 *
 * // Apply the masker to the JSON string
 * String maskedJson = jsonMasker.apply(json);
 *
 * // Output the masked JSON
 * System.out.println(maskedJson);
 * }</pre>
 *
 * The above code would produce the following masked JSON:
 * <pre>{@code
 * {
 *     "name": "********",
 *     "age": 30,
 *     "email": "***************",
 *     "phone": "MTIzLTQ1Ni03ODkw"
 * }
 * }</pre>
 */
@Slf4j
@RequiredArgsConstructor
public final class JsonMasker implements Masker<String> {

    private final boolean prettify;

    private final Map<String, Masker<String>> properties;

    private final ObjectMapper objectMapper;

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Serializes the given object to a JSON string and applies masking to the JSON content.
     *
     * @param value the object to be masked, which will be converted to a JSON string
     * @return the masked JSON string, or {@code null} if the input object is {@code null}
     * @throws MaskingException if fails deserializing input value to JSON content or masking JSON content
     * @implNote Only fields of type {@code String} will be masked. Non-String fields will be ignored.
     */
    @Nullable
    public String applyToObject(@Nullable Object value) {

        if (value == null) {
            log.debug("Input value is null. Returning null.");
            return null;
        }

        try {
            var content = objectMapper.writeValueAsString(value);
            return apply(content);
        } catch (MaskingException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new MaskingException("Failed to deserialize input value as JSON content", ex);
        }
    }

    /**
     * Applies transformations to the given JSON string, such as masking specific fields.
     *
     * @param content the JSON string to be transformed, can be {@code null}
     * @return the transformed JSON string, or {@code null} if the input string was {@code null}
     * @throws MaskingException if fails masking JSON content
     * @implNote Only fields of type {@code String} will be masked. Non-String fields will be ignored.
     */
    @Override
    @Nullable
    public String apply(@Nullable String content) {

        if (content == null) {
            log.debug("Input content is null. Returning null.");
            return null;
        }

        var jsonFactory = new JsonFactory();

        try (var jsonParser = jsonFactory.createParser(new StringReader(content));
                var stringWriter = new StringWriter();
                var jsonGenerator = createGenerator(jsonFactory, stringWriter)) {

            while (!jsonParser.isClosed()) {
                var jsonToken = jsonParser.nextToken();
                if (jsonToken == null) break;

                if (jsonToken == FIELD_NAME) {
                    var fieldName = jsonParser.currentName();
                    jsonGenerator.writeFieldName(fieldName);
                    jsonToken = jsonParser.nextToken();
                    maskFieldValue(fieldName, jsonToken, jsonGenerator, jsonParser);

                } else {
                    jsonGenerator.copyCurrentEvent(jsonParser);
                }
            }

            jsonGenerator.flush();
            return stringWriter.toString();
        } catch (Exception ex) {
            throw new MaskingException("Failed to mask JSON content", ex);
        }
    }

    private JsonGenerator createGenerator(JsonFactory jsonFactory, Writer writer) throws IOException {
        var generator = jsonFactory.createGenerator(writer);

        if (prettify) {
            generator.useDefaultPrettyPrinter();
        }

        return generator;
    }

    private void maskFieldValue(String fieldName, JsonToken token, JsonGenerator generator, JsonParser parser)
            throws IOException {

        var masker = properties.get(fieldName);

        if (masker == null) {
            log.debug("Ignoring field: {}. No masker found.", fieldName);
            generator.copyCurrentEvent(parser);
            return;
        }

        if (token == VALUE_STRING) {
            generator.writeString(masker.apply(parser.getValueAsString()));
            return;
        }

        if (token == START_ARRAY) {
            maskArrayValues(fieldName, masker, generator, parser);
            return;
        }

        log.debug("Ignoring field: {}. Only values of type String will be masked.", fieldName);
        generator.copyCurrentEvent(parser);
    }

    /**
     * Masks string values within a JSON array.
     * This implementation masks:
     * - Direct string elements of the array
     * - String values within nested objects inside the array
     * - String values within nested arrays (recursively)
     * <p>
     * Non-string values (numbers, booleans, nulls) are preserved without masking.
     *
     * @param fieldName the name of the field containing the array
     * @param masker the masking strategy to apply to string values
     * @param generator the JSON generator to write the masked array
     * @param parser the JSON parser to read the array values
     * @throws IOException if an I/O error occurs during JSON processing
     */
    private void maskArrayValues(String fieldName, Masker<String> masker, JsonGenerator generator, JsonParser parser)
            throws IOException {
        generator.writeStartArray();
        var valueToken = parser.nextToken();

        while (valueToken != null && valueToken != END_ARRAY) {
            if (valueToken == VALUE_STRING) {
                generator.writeString(masker.apply(parser.getValueAsString()));
            } else if (valueToken == START_ARRAY) {
                // Recursively process nested arrays
                maskArrayValues(fieldName, masker, generator, parser);
            } else {
                log.debug("Ignoring array: {} value. Only values of type String will be masked.", fieldName);
                generator.copyCurrentEvent(parser);
            }

            valueToken = parser.nextToken();
        }

        generator.writeEndArray();
    }

    /**
     * Builder class for creating instances of {@link JsonMasker}.
     *
     * <p>This builder allows customizing the behavior of the {@link JsonMasker}, such as:
     * <ul>
     *   <li>Defining which JSON properties should be masked</li>
     *   <li>Specifying how properties should be masked using custom or predefined {@link Masker} implementations</li>
     *   <li>Enabling or disabling pretty-printing of the output JSON</li>
     *   <li>Providing a custom {@link ObjectMapper} for JSON serialization/deserialization</li>
     * </ul>
     */
    @SuppressWarnings("NullAway.Init")
    public static final class Builder implements MaskerBuilder<String, JsonMasker> {

        @Nullable
        private ObjectMapper objectMapper;

        private boolean prettify;
        private final Map<String, Masker<String>> properties = new HashMap<>();

        /**
         * Adds properties to be masked with the default {@link FixedLengthMasker}.
         *
         * @param properties the JSON property names to be masked
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if the {@code properties} argument is {@code null}
         */
        public Builder withProperties(String... properties) {
            return withProperties(FixedLengthMasker.builder().build(), properties);
        }

        /**
         * Adds properties to be masked with the masker created by the provided {@link MaskerBuilder}.
         *
         * @param <S> the type of masker created by the builder
         * @param maskerBuilder the builder that will create the masker to be applied to the given properties
         * @param properties the JSON property names to be masked
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code properties} is {@code null}
         */
        public <S extends Masker<String>> Builder withProperties(
                MaskerBuilder<String, S> maskerBuilder, String... properties) {
            return withProperties(Set.of(properties), maskerBuilder.build());
        }

        /**
         * Adds properties to be masked with the specified {@link Masker}.
         *
         * @param masker     the masking strategy to be applied to the given properties
         * @param properties the JSON property names to be masked
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code properties} is {@code null}
         */
        public Builder withProperties(Masker<String> masker, String... properties) {
            return withProperties(Set.of(properties), masker);
        }

        /**
         * Adds properties to be masked with the default {@link FixedLengthMasker}.
         *
         * @param properties a {@link Set} of JSON property names to be masked
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if the {@code properties} argument is {@code null}
         */
        public Builder withProperties(Set<String> properties) {
            return withProperties(properties, FixedLengthMasker.builder().build());
        }

        /**
         * Adds properties to be masked with the masker created by the provided {@link MaskerBuilder}.
         *
         * @param <S> the type of masker created by the builder
         * @param properties a {@link Set} of JSON property names to be masked
         * @param maskerBuilder the builder that will create the masker to be applied to the given properties
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code properties} is {@code null}
         */
        public <S extends Masker<String>> Builder withProperties(
                Set<String> properties, MaskerBuilder<String, S> maskerBuilder) {
            withProperties(properties, maskerBuilder.build());
            return this;
        }

        /**
         * Adds properties to be masked with the specified {@link Masker}.
         *
         * @param properties a {@link Set} of JSON property names to be masked
         * @param masker     the masking strategy to be applied to the given properties
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code properties} is {@code null}
         */
        public Builder withProperties(Set<String> properties, Masker<String> masker) {
            properties.forEach(property -> withProperty(property, masker));
            return this;
        }

        /**
         * Adds a single property to be masked with the default {@link FixedLengthMasker}.
         *
         * @param property the JSON property name to be masked
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if the {@code property} argument is {@code null}
         */
        public Builder withProperty(String property) {
            return withProperty(property, FixedLengthMasker.builder().build());
        }

        /**
         * Adds a single property to be masked with the masker created by the provided {@link MaskerBuilder}..
         *
         * @param <S> the type of masker created by the builder
         * @param property the JSON property name to be masked
         * @param maskerBuilder the builder that will create the masker to be applied to the given properties
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code property} is {@code null}
         */
        public <S extends Masker<String>> Builder withProperty(
                String property, MaskerBuilder<String, S> maskerBuilder) {
            withProperty(property, maskerBuilder.build());
            return this;
        }

        /**
         * Adds a single property to be masked with the specified {@link Masker}.
         *
         * @param property the JSON property name to be masked
         * @param masker   the masking strategy to be applied to the given property
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if either {@code masker} or {@code property} is {@code null}
         */
        public Builder withProperty(String property, Masker<String> masker) {
            properties.put(property, masker);
            return this;
        }

        /**
         * Specifies a custom {@link ObjectMapper} to be used for JSON serialization/deserialization.
         *
         * @param objectMapper the custom {@link ObjectMapper} to be used
         * @return the current {@link Builder} instance for chaining
         * @throws NullPointerException if the {@code objectMapper} argument is {@code null}
         */
        public Builder withObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
            return this;
        }

        /**
         * Enables or disables pretty-printing of the output JSON.
         *
         * @param prettify {@code true} to enable pretty printing, {@code false} to disable it
         * @return the current {@link Builder} instance for chaining
         */
        public Builder prettify(boolean prettify) {
            this.prettify = prettify;
            return this;
        }

        /**
         * Constructs a new {@link JsonMasker} instance and applies transformations to the given JSON string,
         * such as masking specific fields.
         *
         * @param content the JSON string to be transformed, can be {@code null}
         * @return the transformed JSON string, or {@code null} if the input string was {@code null}
         * @see JsonMasker#apply(String)
         */
        @Nullable
        @Override
        public String apply(@Nullable String content) {
            return build().apply(content);
        }

        /**
         * Constructs a new {@link JsonMasker} instance
         * and serializes the given object to a JSON string applying masking to specific fields.
         *
         * @param value the object to be masked, which will be converted to a JSON string
         * @return the masked JSON string, or {@code null} if the input object is {@code null}
         * @see JsonMasker#applyToObject(Object)
         */
        @Nullable
        public String applyToObject(@Nullable Object value) {
            return build().applyToObject(value);
        }

        /**
         * Returns a composed {@link Function} that first applies the current masking operation and then
         * applies the given {@code Masker<String>} operation.
         *
         * @param after the {@code Masker<String>} operation to apply after the current masking operation
         * @return a composed {@code Function<String, String>} that applies the current operation followed by the given one
         * @throws NullPointerException if {@code after} is {@code null}
         */
        @Override
        public Function<String, String> andThen(Masker<String> after) {
            return build().andThen(after);
        }

        /**
         * Builds and returns a new {@link JsonMasker} instance with the configured settings.
         *
         * @return a new {@link JsonMasker} instance
         * @implNote If no {@code ObjectMapper} is explicitly set, a new instance will be created internally.
         */
        @Override
        public JsonMasker build() {
            objectMapper = requireNonNullElseGet(objectMapper, ObjectMapper::new);
            return new JsonMasker(prettify, properties, objectMapper);
        }
    }
}
