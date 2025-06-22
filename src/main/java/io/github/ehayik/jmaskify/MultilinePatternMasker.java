package io.github.ehayik.jmaskify;

import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;

/**
 * Provides functionality to mask portions of a string based on specified multiline regular expressions.
 * <p>
 *     This class is immutable and thread-safe.
 *
 * <p>
 * <strong>Usage Example:</strong>
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

    @Nullable
    private final Pattern multilinePattern;

    private final Map<Pattern, Masker<String>> customMaskerPatterns;

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
     * @param text the input string to be processed, may be {@code null}
     * @return the processed string with substitutions, or {@code null} if the input text was {@code null}
     */
    @Nullable
    @Override
    public String apply(@Nullable String text) {

        if (text == null) {
            log.debug("Input text is null. Returning null.");
            return null;
        }

        return applyMultilinePattern(applyCustomMaskerPatterns(text));
    }

    private String applyCustomMaskerPatterns(String text) {

        if (customMaskerPatterns.isEmpty()) {
            log.debug("No custom masker patterns were specified.");
            return text;
        }

        var result = text;

        for (Map.Entry<Pattern, Masker<String>> entry : customMaskerPatterns.entrySet()) {
            var pattern = entry.getKey();
            var masker = entry.getValue();
            result = applyCustomMaskerPattern(pattern, masker, result);
        }

        return result;
    }

    private String applyCustomMaskerPattern(Pattern pattern, Masker<String> masker, String text) {
        var matcher = pattern.matcher(text);
        var sb = new StringBuilder();

        // Find and replace each match
        while (matcher.find()) {
            var matched = matcher.group(0);
            var masked = masker.apply(matched);
            // Quote the replacement string to avoid issues with special characters
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private String applyMultilinePattern(String text) {

        if (multilinePattern == null) {
            log.debug("No default masker patterns were specified.");
            return text;
        }

        var sb = new StringBuilder(text);
        var matcher = multilinePattern.matcher(sb);

        while (matcher.find()) {
            // If no groups were captured, mask the entire match
            if (matcher.groupCount() == 0) {
                IntStream.range(matcher.start(), matcher.end()).forEach(i -> sb.setCharAt(i, substitution));
            } else {
                IntStream.rangeClosed(1, matcher.groupCount()).forEach(group -> {
                    if (matcher.group(group) != null) {
                        IntStream.range(matcher.start(group), matcher.end(group))
                                .forEach(i -> sb.setCharAt(i, substitution));
                    }
                });
            }
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
    public static final class Builder implements MaskerBuilder<String, MultilinePatternMasker> {

        private char substitution = DEF_SUBSTITUTION_CHAR;
        private final Set<String> defaultMaskerPatterns = new HashSet<>();
        private final Map<String, Masker<String>> customMaskerPatterns = new HashMap<>();

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

            defaultMaskerPatterns.add(maskPattern);
            return this;
        }

        /**
         * Adds a new regular expression pattern with a specific masking strategy.
         *
         * @param pattern the regular expression pattern used for matching sensitive data
         * @param masker the masking strategy to apply to the matched pattern
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code pattern} is {@code null} or blank
         * @throws NullPointerException if {@code masker} is {@code null}
         */
        public Builder withMaskPattern(String pattern, Masker<String> masker) {

            if (isBlank(pattern)) {
                throw new IllegalArgumentException("Pattern cannot be blank");
            }

            //            defaultMaskerPatterns.add(pattern);
            customMaskerPatterns.put(pattern, masker);
            return this;
        }

        /**
         * Adds a new regular expression pattern with a specific masking strategy builder.
         * The builder will be built to get the actual masker.
         *
         * @param pattern the regular expression pattern used for matching sensitive data
         * @param maskerBuilder the masking strategy builder to build and apply to the matched pattern
         * @return the builder instance for chaining method calls
         * @throws IllegalArgumentException if {@code pattern} is {@code null} or blank
         * @throws NullPointerException if {@code maskerBuilder} is {@code null}
         */
        public Builder withMaskPattern(String pattern, MaskerBuilder<String, ?> maskerBuilder) {

            if (isBlank(pattern)) {
                throw new IllegalArgumentException("Pattern cannot be blank");
            }

            return withMaskPattern(pattern, maskerBuilder.build());
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
         * Constructs a new {@link MultilinePatternMasker} instance
         * and applies the specified multiline pattern to the given text.
         *
         * <p>
         *      Each match found by the patterns is replaced with a specified substitution character.
         *
         * @param text the input string to be processed, may be {@code null}
         * @return the processed string with substitutions, or {@code null} if the input text was {@code null}
         * @see MultilinePatternMasker#apply(String)
         */
        @Nullable
        @Override
        public String apply(@Nullable String text) {
            return build().apply(text);
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
         * Constructs and returns a new {@link MultilinePatternMasker} instance
         * based on the configured patterns and substitution character.
         *
         * @return a new {@link MultilinePatternMasker} instance
         * @throws MaskingException if no patterns were added to the builder
         * @implNote All the specified mask patterns will be combined into a single pattern
         *          using the {@link Pattern#MULTILINE} flag.
         */
        @Override
        public MultilinePatternMasker build() {

            if (defaultMaskerPatterns.isEmpty()) {
                throw new MaskingException(
                        "At least one mask pattern must be specified. Use withMaskPattern() to add patterns.");
            }

            return new MultilinePatternMasker(substitution, compileMultilinePattern(), compileCustomMaskerPatterns());
        }

        @Nullable
        private Pattern compileMultilinePattern() {

            if (defaultMaskerPatterns.isEmpty()) {
                return null;
            }

            return Pattern.compile(String.join("|", defaultMaskerPatterns), Pattern.MULTILINE);
        }

        private Map<Pattern, Masker<String>> compileCustomMaskerPatterns() {
            return customMaskerPatterns.entrySet().stream()
                    .collect(toMap(entry -> Pattern.compile(entry.getKey()), Map.Entry::getValue));
        }
    }
}
