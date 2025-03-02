package io.github.ehayik.jmaskify;

import java.util.function.Function;

/**
 * Provides functionality to mask String values based on various masking strategies.
 *
 * <p>
 *      It extends the {@link Function} interface,
 *      with the input type being a generic and the output type being String.
 * <p>
 *      This interface permits the following subclasses:
 *          <ul>
 *              <li>{@link MultilinePatternMasker}</li>
 *              <li>{@link JsonMasker}</li>
 *              <li>{@link FixedLengthMasker}</li>
 *              <li>{@link Base64Masker}</li>
 *          </ul>
 * <p>
 *      Static factory methods are provided for the convenience of creating
 *      different Maskers.
 *
 * <p>
 * <h6>Masking sensitive fields within JSON strings:</h6>
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
 * JsonMasker jsonMasker = Masker.json()
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
 *
 * <p>
 * <h6>Finding and masking specific patterns in text:</h6>
 * <pre>
 * {@code
 * // Define patterns to mask sensitive data
 * String usernamePattern = "username\\s*=\\s*([^\\s]+)";
 * String ipAddressPattern = "(\\d+\\.\\d+\\.\\d+\\.\\d+)";
 *
 * // Create an instance of MultilinePatternMasker using the builder
 * MultilinePatternMasker masker = MultilinePatternMasker.builder()
 *     .withMaskPattern(usernamePattern)
 *     .withMaskPattern(ipAddressPattern)
 *     .withSubstitution('*')  // Define the substitution character
 *     .build();
 *
 * // Input string with sensitive data spread across multiple lines
 * String input = "Line 1 with username=johndoe sensitive data.\n"
 *              + "Line 2 192.168.1.1 with more sensitive info.";
 *
 * // Apply the masker to the input
 * String maskedOutput = masker.apply(input);
 *
 * // Result: sensitive data is masked, i.e.,
 * // Line 1 with username=******* sensitive data.
 * // Line 2 ********** with more sensitive info.
 * }
 * </pre>
 *
 * @param <T> the generic type parameter which the mask operation is applied to
 */
public sealed interface Masker<T> extends Function<T, String>
        permits MultilinePatternMasker, JsonMasker, FixedLengthMasker, Base64Masker, DelegateMasker, CreditCardMasker {

    /**
     * The default substitution character used for masking operations.
     *
     * <p>
     *      This character is used to replace characters in strings
     *      when applying masking transformations, providing a standardized
     *      placeholder for obfuscated characters.
     */
    char DEF_SUBSTITUTION_CHAR = '*';

    /**
     * Creates a masker that masks strings to a fixed length with a substitution character.
     *
     * @return a <code>Masker</code> that masks strings to the same length as the input string
     */
    static Masker<String> fixedLength() {
        return new FixedLengthMasker();
    }

    /**
     * Creates a masker that masks strings to a fixed length with a substitution character.
     *
     * @param fixedLength the length to which the input string should be masked.
     *                    If the value is zero or negative, the input string is masked to the same length as the input string.
     * @return a Masker that replaces the input string with a fixed number of substitution characters.
     */
    static Masker<String> fixedLength(int fixedLength) {
        return new FixedLengthMasker(fixedLength);
    }

    /**
     * Creates a masker that applies Base64 encoding to the input string.
     *
     * @return a <code>Masker</code> that converts the input string to its Base64 encoded representation.
     */
    static Masker<String> base64() {
        return new Base64Masker();
    }

    /**
     * Creates a builder for constructing a {@link JsonMasker} instance.
     *
     * @return a {@link JsonMasker.Builder} instance for customizable JSON masking strategy
     */
    static JsonMasker.Builder json() {
        return JsonMasker.builder();
    }

    /**
     * Creates a builder for constructing a {@link MultilinePatternMasker} instance.
     *
     * @return a {@link MultilinePatternMasker.Builder} instance for customizable multiline pattern masking strategy
     */
    static MultilinePatternMasker.Builder multilinePattern() {
        return MultilinePatternMasker.builder();
    }

    /**
     * Creates a masker that delegates the masking operation to the provided function.
     *
     * @param <S> the type of the input to the masker
     * @param delegate the function to delegate the masking operation to
     * @return a <code>Masker</code> that uses the provided function for its masking operation
     */
    static <S> Masker<S> delegate(Function<S, String> delegate) {
        return new DelegateMasker<>(delegate);
    }

    static Masker<String> creditCard() {
        return new CreditCardMasker();
    }
}
