package io.github.ehayik.jmaskify;

import static org.apache.commons.lang3.StringUtils.isBlank;

import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides functionality to mask portions of a string based on specified multiline regular expressions.
 * <p>
 *     This class is immutable and thread-safe.
 *
 * <p>
 * <h6>Usage Example:</h6>
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
 */
@Slf4j
@RequiredArgsConstructor
public final class MultilinePatternMasker implements Masker<String> {

    private final char substitution;
    private final Pattern multilinePattern;

    public static MultilinePatternMasker.Builder builder() {
        return new MultilinePatternMasker.Builder();
    }

    /**
     * Applies the specified multiline pattern to the given text,
     * replacing all matched portions with the substitution character.
     *
     * <p>
     *      Each match found by the patterns is replaced with a specified substitution character.
     *
     * @param text the input string to be processed; may be {@code null}
     * @return the processed string with substitutions, or {@code null} if the input text was {@code null}
     */
    @Nullable
    @Override
    public String apply(@Nullable String text) {

        if (text == null) {
            log.debug("Input text is null. Returning null.");
            return null;
        }

        var sb = new StringBuilder(text);
        var matcher = multilinePattern.matcher(sb);

        while (matcher.find()) {
            IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                if (matcher.group(group) != null) {
                    IntStream.range(matcher.start(group), matcher.end(group))
                            .forEach(i -> sb.setCharAt(i, substitution));
                }
            });
        }

        return sb.toString();
    }

    /**
     * A builder class for creating instances of {@link MultilinePatternMasker}.
     * <p>
     * This builder provides methods to specify the masking patterns and substitution characters
     * to be used when constructing a {@link MultilinePatternMasker}.
     * It allows a flexible and intuitive approach to configuring masker objects through fluent API calls.
     */
    public static final class Builder {

        private char substitution = DEF_SUBSTITUTION_CHAR;
        private final List<String> maskPatterns = new ArrayList<>();

        /**
         * Adds a new regular expression pattern to the list of patterns that will
         * be used for masking sensitive data.
         *
         * @param maskPattern the regular expression pattern used for matching sensitive data
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code maskPattern} is {@code null} or blank
         */
        public Builder withMaskPattern(String maskPattern) {

            if (isBlank(maskPattern)) {
                throw new IllegalArgumentException("Mask pattern cannot be blank");
            }

            maskPatterns.add(maskPattern);
            return this;
        }

        /**
         * Sets the substitution character to be used for replacing matched sensitive data.
         *
         * <p>
         *      If not explicitly set, the default substitution character defined in the implementation
         *      will be used.
         *
         * @param substitution the character used for substitution
         * @return the builder instance for chaining method calls
         */
        public Builder withSubstitution(char substitution) {
            this.substitution = substitution;
            return this;
        }

        /**
         * Constructs and returns a new {@link MultilinePatternMasker} instance
         * based on the configured patterns and substitution character.
         *
         * @return a new {@link MultilinePatternMasker} instance
         * @throws MaskingException if no patterns were added to the builder
         * @implNote All the specified mask patterns will be combined into a single pattern
         *          using the {@link Pattern#MULTILINE} flag.
         */
        public MultilinePatternMasker build() {

            if (maskPatterns.isEmpty()) {
                throw new MaskingException("Mask patterns cannot be empty");
            }

            var multilinePattern = Pattern.compile(String.join("|", maskPatterns), Pattern.MULTILINE);
            return new MultilinePatternMasker(substitution, multilinePattern);
        }
    }
}
